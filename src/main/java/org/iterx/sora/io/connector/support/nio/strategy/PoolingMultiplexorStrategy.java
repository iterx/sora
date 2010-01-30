package org.iterx.sora.io.connector.support.nio.strategy;

import org.iterx.sora.io.IoException;
import org.iterx.sora.realtime.thread.RealtimeThread;
import org.iterx.sora.realtime.thread.RealtimeThreadPool;
import org.iterx.sora.util.collection.Set;

import java.io.IOException;
import java.nio.channels.Channel;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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

    public static PoolingMultiplexorStrategy<?> newSinglePoolMultiplexorStrategy(final RealtimeThreadPool realtimeThreadPool) {
        final Worker readWriteOpenCloseWorker = new Worker(realtimeThreadPool, READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexorStrategy(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static PoolingMultiplexorStrategy<?> newOpenReadWritePoolMultiplexorStrategy(final RealtimeThreadPool realtimeThreadPool) {
        final Worker readWorker = new Worker(realtimeThreadPool, READ_OP);
        final Worker writeWorker = new Worker(realtimeThreadPool, WRITE_OP);
        final Worker openCloseWorker = new Worker(realtimeThreadPool, OPEN_OP|CLOSE_OP);
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

        private final RealtimeThread realtimeThread;
        private final CountDownLatch startSignal;
        private final CountDownLatch destroySignal;

        private final Multiplexor<? extends Channel> multiplexor;

        private final long pollTime = 1000L; //TODO: read in via properties or self-tune

        private Worker(final RealtimeThreadPool realtimeThreadPool,
                       final int validOps) {
            this.realtimeThread = realtimeThreadPool.newThread(this);
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            this.multiplexor = new SelectableChannelMultiplexor(validOps);
            init();
        }

        private void init() {
             try {
                realtimeThread.start();
                startSignal.await();
            }
            catch(final Exception e) {
                throw rethrow(e);
            }
        }

        public void run() {
              try {
                  startSignal.countDown();
                  while(!Thread.currentThread().isInterrupted()) multiplexor.poll(pollTime, TimeUnit.MILLISECONDS);
              }
              finally {
                  destroySignal.countDown();
              }
        }

        public void register(final MultiplexorHandler<? extends Channel> multiplexorHandler, final int ops) {
            getMultiplexor().register(multiplexorHandler, ops);
        }

        public void deregister(final MultiplexorHandler<? extends Channel> multiplexorHandler, final int ops) {
            getMultiplexor().deregister(multiplexorHandler, ops);
        }

        public void destroy() {
            try {
                if(destroySignal.getCount() != 0) {
                    try {
                        try {
                            multiplexor.destroy();
                        }
                        catch(final Throwable throwable) {
                            swallow(throwable);
                        }
                     }
                    finally {
                        realtimeThread.interrupt();
                    }
                }
                destroySignal.await();
            }
            catch(final InterruptedException e) {
                swallow(e);
            }
        }

        @SuppressWarnings("unchecked")
        private <T extends Channel> Multiplexor<T> getMultiplexor() {
            return (Multiplexor<T>) multiplexor;
        }
    }

    private static abstract class Multiplexor<T extends Channel> {

        abstract void poll(long time, TimeUnit timeUnit);

        abstract void register(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

        abstract void deregister(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

        abstract void destroy();
    }

    private static final class FileChannelMultiplexor extends Multiplexor<java.nio.channels.FileChannel> {

        //TODO: do we want to allow file & socket multiplexors on same thread???

        private volatile MultiplexorHandler[] readMultiplexorHandlers;
        private volatile MultiplexorHandler[] writeMultiplexorHandlers;

        public FileChannelMultiplexor(final int validOps) {
        }

        public void poll(final long time, final TimeUnit timeUnit) {
/*
            final Handler[] handlers = getHandlers();
            try {
                int written = 0;
                int read = 0;
                for(final Handler handler : handlers) {
                    written += ((ops & WRITE) != 0)? handler.doWrite(1024) : 0;
                    read += ((ops & READ) != 0)? handler.doRead(1024) : 0;
                }
                if(written == 0 && read == 0) await(time, timeUnit);
            }
            catch(final Throwable throwable) {
                throw rethrow(throwable);
            }
*/
        }

        public void register(final MultiplexorHandler<? extends FileChannel> multiplexorHandler, final int ops) {

        }

        public void deregister(final MultiplexorHandler<? extends FileChannel> multiplexorHandler, final int ops) {

        }

        public void destroy() {

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

        @Override
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
            catch(final Throwable throwable) {
                throw rethrow(throwable);
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
