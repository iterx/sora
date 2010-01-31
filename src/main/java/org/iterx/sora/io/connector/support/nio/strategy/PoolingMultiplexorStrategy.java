package org.iterx.sora.io.connector.support.nio.strategy;

import org.iterx.sora.io.IoException;
import org.iterx.sora.collection.Arrays;

import java.io.IOException;
import java.nio.channels.Channel;
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


public final class PoolingMultiplexorStrategy<T extends Channel> implements MultiplexorStrategy<T> {

    private final Worker openCloseWorker;
    private final Worker writeWorker;
    private final Worker readWorker;

    public PoolingMultiplexorStrategy(final Worker openCloseWorker,
                                      final Worker writeWorker,
                                      final Worker readWorker) {
        this.openCloseWorker = openCloseWorker;
        this.writeWorker = writeWorker;
        this.readWorker = readWorker;
    }

    public static PoolingMultiplexorStrategy<?> newSinglePoolMultiplexorStrategy(final ThreadFactory threadFactory) {
        final Worker readWriteOpenCloseWorker = new Worker(threadFactory, READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexorStrategy(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static PoolingMultiplexorStrategy<?> newOpenReadWritePoolMultiplexorStrategy(final ThreadFactory threadFactory) {
        final Worker readWorker = new Worker(threadFactory, READ_OP);
        final Worker writeWorker = new Worker(threadFactory, WRITE_OP);
        final Worker openCloseWorker = new Worker(threadFactory, OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexorStrategy(openCloseWorker, writeWorker, readWorker);
    }

    @Override
    public void register(final MultiplexorHandler<? extends T> multiplexorHandler, final int ops) {
        if((ops & READ_OP) != 0) readWorker.register(multiplexorHandler, READ_OP);
        if((ops & WRITE_OP) != 0) writeWorker.register(multiplexorHandler, WRITE_OP);
        if((ops & OPEN_OP) != 0 || (ops & CLOSE_OP) != 0) openCloseWorker.register(multiplexorHandler, OPEN_OP|CLOSE_OP);
    }

    @Override
    public void deregister(final MultiplexorHandler<? extends T> multiplexorHandler, final int ops) {
        if((ops & OPEN_OP) != 0 || (ops & CLOSE_OP) != 0) openCloseWorker.deregister(multiplexorHandler, OPEN_OP);
        if((ops & WRITE_OP) != 0) writeWorker.deregister(multiplexorHandler, READ_OP);
        if((ops & READ_OP) != 0 ) readWorker.deregister(multiplexorHandler, WRITE_OP);
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

        public void register(final MultiplexorHandler<? extends Channel> multiplexorHandler, final int ops) {
            getMultiplexor(multiplexorHandler).register(multiplexorHandler, ops);
        }

        public void deregister(final MultiplexorHandler<? extends Channel> multiplexorHandler, final int ops) {
            getMultiplexor(multiplexorHandler).deregister(multiplexorHandler, ops);
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
        private <T extends Channel> Multiplexor<T> getMultiplexor(final MultiplexorHandler<?> multiplexorHandler) {
            for(final Multiplexor<? extends Channel> multiplexor : multiplexors) {
                if(multiplexor.supports(multiplexorHandler)) return (Multiplexor<T>) multiplexor;
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

        abstract boolean supports(MultiplexorHandler<?> multiplexorHandler);

        abstract boolean isReady();

        abstract void poll(long time, TimeUnit timeUnit);

        abstract void register(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

        abstract void deregister(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

        abstract void destroy();
    }

    private static final class FileChannelMultiplexor extends Multiplexor<java.nio.channels.FileChannel> {

        private final int validOps;
        private final Lock lock;

        private volatile MultiplexorHandler<? extends java.nio.channels.FileChannel>[] readMultiplexorHandlers;
        private volatile MultiplexorHandler<? extends java.nio.channels.FileChannel>[] writeMultiplexorHandlers;

        public FileChannelMultiplexor(final int validOps) {
            this.readMultiplexorHandlers = Arrays.newArray(MultiplexorHandler.class, 0);
            this.writeMultiplexorHandlers = Arrays.newArray(MultiplexorHandler.class, 0);
            this.lock = new ReentrantLock();
            this.validOps = validOps;
        }

        public boolean supports(final MultiplexorHandler<?> multiplexorHandler) {
            return multiplexorHandler.getChannel() instanceof java.nio.channels.FileChannel;
        }

        public boolean isReady() {
            if(writeMultiplexorHandlers.length == 0) {
                if(readMultiplexorHandlers.length != 0) {
                    for(final MultiplexorHandler<? extends java.nio.channels.FileChannel> readMultiplexorHandler : readMultiplexorHandlers) {
                        try {
                            final java.nio.channels.FileChannel fileChannel = readMultiplexorHandler.getChannel();
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

        public void register(final MultiplexorHandler<? extends java.nio.channels.FileChannel> multiplexorHandler, final int ops) {
            if((ops & validOps) != 0) {
                lock.lock();
                try {
                    if((ops & READ_OP) != 0) readMultiplexorHandlers = Arrays.add(readMultiplexorHandlers, multiplexorHandler);
                    if((ops & WRITE_OP) != 0) writeMultiplexorHandlers = Arrays.add(writeMultiplexorHandlers, multiplexorHandler);
                }
                finally {
                    lock.unlock();
                }
            }
        }

        public void deregister(final MultiplexorHandler<? extends java.nio.channels.FileChannel> multiplexorHandler, final int ops) {
            if((ops & validOps) != 0) {
                lock.lock();
                try {
                    if((ops & READ_OP) != 0) readMultiplexorHandlers = Arrays.remove(readMultiplexorHandlers, multiplexorHandler);
                    if((ops & WRITE_OP) != 0) writeMultiplexorHandlers = Arrays.remove(writeMultiplexorHandlers, multiplexorHandler);
                }
                finally {
                    lock.unlock();
                }
            }
        }

        public void destroy() {
            lock.lock();
            try {
                readMultiplexorHandlers = Arrays.newArray(MultiplexorHandler.class, 0);
                writeMultiplexorHandlers = Arrays.newArray(MultiplexorHandler.class, 0);
            }
            finally {
                lock.unlock();
            }
        }

        private void doRead() {
            final MultiplexorHandler[] multiplexorHandlers = readMultiplexorHandlers;
            for(final MultiplexorHandler multiplexorHandler : multiplexorHandlers) multiplexorHandler.doRead(1024);
        }

        private void doWrite() {
            final MultiplexorHandler[] multiplexorHandlers = writeMultiplexorHandlers;
            for(final MultiplexorHandler multiplexorHandler : multiplexorHandlers) multiplexorHandler.doWrite(1024);
        }
    }

    private static final class SelectableChannelMultiplexor extends Multiplexor<SelectableChannel> {

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

        public boolean supports(final MultiplexorHandler<?> multiplexorHandler) {
            return multiplexorHandler.getChannel() instanceof SelectableChannel;
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
                            final MultiplexorHandler<SelectableChannel> multiplexorHandler = (MultiplexorHandler<SelectableChannel>) selectionKey.attachment();
                            final SelectableChannel selectableChannel = selectionKey.channel();
                            if(selectableChannel.isOpen()) {
                                final int readyOps = selectionKey.readyOps() & selectorOps;
                                if((readyOps & SelectionKey.OP_CONNECT) != 0) multiplexorHandler.doOpen();
                                if((readyOps & SelectionKey.OP_ACCEPT) != 0) multiplexorHandler.doOpen();
                                if((readyOps & SelectionKey.OP_READ) != 0) multiplexorHandler.doRead(readPollSize);
                                if((readyOps & SelectionKey.OP_WRITE) != 0) multiplexorHandler.doWrite(writePollSize);
                            }
                            else  {
                                if((selectorOps & OP_CLOSE) == 0) multiplexorHandler.doClose();
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

        public void register(final MultiplexorHandler<? extends SelectableChannel> multiplexorHandler, final int ops) {
            try {
                final SelectableChannel selectableChannel = multiplexorHandler.getChannel();
                final int interestOps = toSelectorOps(ops) & selectorOps & selectableChannel.validOps();
                if(interestOps != 0) {
                     SelectionKey selectionKey = selectableChannel.keyFor(selector);
                    if(selectionKey != null && selectionKey.isValid()) {
                        selectionKey.interestOps(selectionKey.interestOps() | interestOps);
                    }
                    else {
                        pendingRegister.incrementAndGet();
                        try {
                            selector.wakeup();
                            selectableChannel.register(selector, interestOps, multiplexorHandler);
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

        public void deregister(final MultiplexorHandler<? extends SelectableChannel> multiplexorHandler, final int ops) {
            final SelectableChannel selectableChannel = multiplexorHandler.getChannel();
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
