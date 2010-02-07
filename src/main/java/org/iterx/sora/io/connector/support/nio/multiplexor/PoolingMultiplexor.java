package org.iterx.sora.io.connector.support.nio.multiplexor;

import org.iterx.sora.io.IoException;
import org.iterx.sora.collection.Arrays;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.Multiplexor;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class PoolingMultiplexor<T extends NioChannel> implements Multiplexor<T> {

    private final Worker openCloseWorker;
    private final Worker writeWorker;
    private final Worker readWorker;

    public PoolingMultiplexor(final Worker openCloseWorker,
                              final Worker writeWorker,
                              final Worker readWorker) {
        this.openCloseWorker = openCloseWorker;
        this.writeWorker = writeWorker;
        this.readWorker = readWorker;
    }

    public static PoolingMultiplexor<?> newSinglePoolMultiplexorStrategy(final ThreadFactory threadFactory) {
        final Worker readWriteOpenCloseWorker = new Worker(threadFactory, READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static PoolingMultiplexor<?> newOpenReadWritePoolMultiplexorStrategy(final ThreadFactory threadFactory) {
        final Worker readWorker = new Worker(threadFactory, READ_OP);
        final Worker writeWorker = new Worker(threadFactory, WRITE_OP);
        final Worker openCloseWorker = new Worker(threadFactory, OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor(openCloseWorker, writeWorker, readWorker);
    }

    @Override
    public void register(final Handler<? extends T> handler, final int ops) {
        if((ops & READ_OP) != 0) readWorker.register(handler, READ_OP);
        if((ops & WRITE_OP) != 0) writeWorker.register(handler, WRITE_OP);
        if((ops & OPEN_OP) != 0 || (ops & CLOSE_OP) != 0) openCloseWorker.register(handler, OPEN_OP|CLOSE_OP);
    }

    @Override
    public void deregister(final Handler<? extends T> handler, final int ops) {
        if((ops & OPEN_OP) != 0 || (ops & CLOSE_OP) != 0) openCloseWorker.deregister(handler, OPEN_OP);
        if((ops & WRITE_OP) != 0) writeWorker.deregister(handler, READ_OP);
        if((ops & READ_OP) != 0 ) readWorker.deregister(handler, WRITE_OP);
    }

    @Override
    public void destroy() {
        openCloseWorker.destroy();
        writeWorker.destroy();
        readWorker.destroy();
    }

    private static final class Worker implements Runnable {

        private final Thread thread;
        private final CountDownLatch startSignal;
        private final CountDownLatch destroySignal;

        private final Multiplexor<? extends Channel>[] multiplexors;

        private final long pollTime = 1000L; //TODO: read in via properties or self-tune

        private Worker(final ThreadFactory threadFactory,
                       final int validOps) {
            this.thread = threadFactory.newThread(this);
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            this.multiplexors = Arrays.newArray(Multiplexor.class,
                                                new FileChannelMultiplexor(validOps),
                                                new SelectableChannelMultiplexor(validOps));
            init();
        }

        private void init() {
             try {
                thread.start();
                startSignal.await();
            }
            catch(final Exception e) {
                throw rethrow(e);
            }
        }

        public void run() {
              try {
                  startSignal.countDown();
                  while(!Thread.currentThread().isInterrupted()) {
                      boolean ready = false;
                      //TODO: add signalling/wakeup from selectors...
                      for(final Multiplexor multiplexor : multiplexors) {
                          if(multiplexor.isReady()) {
                              multiplexor.poll(pollTime, TimeUnit.MILLISECONDS);
                              ready = true;
                          }
                      }
                      if(!ready) sleep(pollTime, TimeUnit.MILLISECONDS);
                  }
              }
              finally {
                  destroySignal.countDown();
              }
        }

        public void register(final Handler<? extends Channel> handler, final int ops) {
            getMultiplexor(handler).register(handler, ops);
        }

        public void deregister(final Handler<? extends Channel> handler, final int ops) {
            getMultiplexor(handler).deregister(handler, ops);
        }

        public void destroy() {
            try {
                if(destroySignal.getCount() != 0) {
                    try {
                        try {
                            for(final Multiplexor multiplexor : multiplexors) multiplexor.destroy();
                        }
                        catch(final Throwable throwable) {
                            swallow(throwable);
                        }
                     }
                    finally {
                        thread.interrupt();
                    }
                }
                destroySignal.await();
            }
            catch(final InterruptedException e) {
                swallow(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Channel> Multiplexor<T> getMultiplexor(final Handler<?> handler) {
            for(final Multiplexor<? extends Channel> multiplexor : multiplexors) {
                if(multiplexor.supports(handler)) return (Multiplexor<T>) multiplexor;
            }
            throw new UnsupportedOperationException();
        }

        private void sleep(final long time, final TimeUnit timeUnit) {
            try {
                Thread.sleep(timeUnit.toMillis(time));
            }
            catch(final InterruptedException e) {
                swallow(e);
            }
        }
    }

    private static abstract class Multiplexor<T extends Channel> {

        abstract boolean supports(Handler<?> handler);

        abstract boolean isReady();

        abstract void poll(long time, TimeUnit timeUnit);

        abstract void register(Handler<? extends T> handler, int ops);

        abstract void deregister(Handler<? extends T> handler, int ops);

        abstract void destroy();
    }

    private static final class FileChannelMultiplexor extends Multiplexor<NioChannel<FileChannel>> {

        private final int validOps;
        private final Lock lock;

        private volatile Handler<? extends NioChannel<FileChannel>>[] readHandlers;
        private volatile Handler<? extends NioChannel<FileChannel>>[] writeHandlers;

        public FileChannelMultiplexor(final int validOps) {
            this.readHandlers = Arrays.newArray(Handler.class, 0);
            this.writeHandlers = Arrays.newArray(Handler.class, 0);
            this.lock = new ReentrantLock();
            this.validOps = validOps;
        }

        public boolean supports(final Handler<?> handler) {
            final Channel channel = handler.getChannel();
            return channel instanceof NioChannel &&
                   ((NioChannel) channel).getChannel() instanceof FileChannel;
        }

        public boolean isReady() {
            if(writeHandlers.length == 0) {
                if(readHandlers.length != 0) {
                    for(final Handler<? extends NioChannel<FileChannel>> readHandler : readHandlers) {
                        try {
                            final NioChannel<FileChannel> channel = readHandler.getChannel();
                            final java.nio.channels.FileChannel fileChannel = channel.getChannel();
                            if(fileChannel.position() < fileChannel.size()) return true;
                        }
                        catch(final IOException e) {
                            swallow(e);
                        }
                    }
                }
                return false;
            }
            return true;
        }

        public void poll(final long time, final TimeUnit timeUnit) {
            doWrite();
            doRead();
        }

        public void register(final Handler<? extends NioChannel<FileChannel>> handler, final int ops) {
            if((ops & validOps) != 0) {
                lock.lock();
                try {
                    if((ops & READ_OP) != 0) readHandlers = Arrays.add(readHandlers, handler);
                    if((ops & WRITE_OP) != 0) writeHandlers = Arrays.add(writeHandlers, handler);
                }
                finally {
                    lock.unlock();
                }
            }
        }

        public void deregister(final Handler<? extends NioChannel<FileChannel>> handler, final int ops) {
            if((ops & validOps) != 0) {
                lock.lock();
                try {
                    if((ops & READ_OP) != 0) readHandlers = Arrays.remove(readHandlers, handler);
                    if((ops & WRITE_OP) != 0) writeHandlers = Arrays.remove(writeHandlers, handler);
                }
                finally {
                    lock.unlock();
                }
            }
        }

        public void destroy() {
            lock.lock();
            try {
                readHandlers = Arrays.newArray(Handler.class, 0);
                writeHandlers = Arrays.newArray(Handler.class, 0);
            }
            finally {
                lock.unlock();
            }
        }

        private void doRead() {
            final Handler[] handlers = readHandlers;
            for(final Handler handler : handlers) handler.doRead(1024);
        }

        private void doWrite() {
            final Handler[] handlers = writeHandlers;
            for(final Handler handler : handlers) handler.doWrite(1024);
        }
    }

    private static final class SelectableChannelMultiplexor extends Multiplexor<NioChannel<? extends SelectableChannel>> {

        private static final int OP_CLOSE = 2;

        private final Selector selector;
        private final int selectorOps;

        private final int readPollSize = 4096; //TODO: read in via properties.... or self tune???
        private final int writePollSize = 4096;

        private final AtomicInteger pendingRegister;

        public SelectableChannelMultiplexor(final int validOps) {
            try {
                this.pendingRegister = new AtomicInteger();
                this.selector = Selector.open();
                this.selectorOps = toSelectorOps(validOps);
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public boolean supports(final Handler<?> handler) {
            final Channel channel = handler.getChannel();
            return channel instanceof NioChannel &&
                   ((NioChannel) channel).getChannel() instanceof SelectableChannel;
        }

        public boolean isReady() {
            return !selector.keys().isEmpty();
        }

        @SuppressWarnings("unchecked")
        public void poll(final long time, final TimeUnit timeUnit) {
            try {
                if(selector.select(timeUnit.toMillis(time)) != 0) {
                    for(final Iterator<SelectionKey> selectionKeyIterator = selector.selectedKeys().iterator();
                        selectionKeyIterator.hasNext();) {
                        try {
                            final SelectionKey selectionKey = selectionKeyIterator.next();
                            final Handler<? extends SelectableChannel> handler = (Handler<? extends SelectableChannel>) selectionKey.attachment();
                            final SelectableChannel selectableChannel = selectionKey.channel();
                            if(selectableChannel.isOpen()) {
                                final int readyOps = selectionKey.readyOps() & selectorOps;
                                if((readyOps & SelectionKey.OP_CONNECT) != 0) handler.doOpen();
                                if((readyOps & SelectionKey.OP_ACCEPT) != 0) handler.doOpen();
                                if((readyOps & SelectionKey.OP_READ) != 0) handler.doRead(readPollSize);
                                if((readyOps & SelectionKey.OP_WRITE) != 0) handler.doWrite(writePollSize);
                            }
                            else  {
                                if((selectorOps & OP_CLOSE) == 0) handler.doClose();
                                selectionKey.cancel();
                            }
                        }
                        finally {
                            selectionKeyIterator.remove();
                        }
                    }
                }
                while(pendingRegister.get() != 0) Thread.yield();
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public void register(final Handler<? extends NioChannel<? extends SelectableChannel>> handler, final int ops) {
            try {
                final NioChannel<? extends SelectableChannel> channel = handler.getChannel();
                final SelectableChannel selectableChannel = channel.getChannel();
                final int interestOps = toSelectorOps(ops) & selectorOps & selectableChannel.validOps();
                if(interestOps != 0) {
                    final SelectionKey selectionKey = selectableChannel.keyFor(selector);
                    if(selectionKey != null && selectionKey.isValid()) {
                        selectionKey.interestOps(selectionKey.interestOps() | interestOps);
                    }
                    else {
                        pendingRegister.incrementAndGet();
                        try {
                            selector.wakeup();
                            selectableChannel.register(selector, interestOps, handler);
                        }
                        finally {
                            pendingRegister.decrementAndGet();
                        }
                    }
                }
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public void deregister(final Handler<? extends NioChannel<? extends SelectableChannel>> handler, final int ops) {
            final NioChannel<? extends SelectableChannel> channel = handler.getChannel();
            final SelectableChannel selectableChannel = channel.getChannel();
            final SelectionKey selectionKey = selectableChannel.keyFor(selector);
            if(selectionKey != null && selectionKey.isValid()) {
                final int interestOps = (toSelectorOps(ops) & selectorOps & selectableChannel.validOps()) ^ selectionKey.interestOps();
                selectionKey.interestOps(interestOps);
            }
        }

        public void destroy() {
            try {
                selector.close();
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        private static int toSelectorOps(final int ops) {
            int selectorOps = 0;
            if((ops & OPEN_OP) != 0) selectorOps |= (SelectionKey.OP_ACCEPT|SelectionKey.OP_CONNECT);
            if((ops & READ_OP) != 0) selectorOps |= SelectionKey.OP_READ;
            if((ops & WRITE_OP) != 0) selectorOps |= SelectionKey.OP_WRITE;
            if((ops & CLOSE_OP) != 0) selectorOps |= OP_CLOSE;
            return selectorOps;
        }
    }
}
