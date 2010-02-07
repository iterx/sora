package org.iterx.sora.io.connector.multiplexor;

import org.iterx.sora.collection.Arrays;
import org.iterx.sora.io.connector.session.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class PoolingMultiplexor <T extends Channel<?>> implements Multiplexor<T> {

    private final Worker openCloseWorker;
    private final Worker writeWorker;
    private final Worker readWorker;

    private PoolingMultiplexor(final Worker openCloseWorker,
                               final Worker writeWorker,
                               final Worker readWorker) {
        this.openCloseWorker = openCloseWorker;
        this.writeWorker = writeWorker;
        this.readWorker = readWorker;
    }

    //TODO: add support to pass in selectors.....
    public static PoolingMultiplexor<?> newSinglePoolingMultiplexor(final ThreadFactory threadFactory) {
        final Worker readWriteOpenCloseWorker = new Worker(threadFactory, READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static PoolingMultiplexor<?> newOpenReadWritePoolingMultiplexor(final ThreadFactory threadFactory) {
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

        private final Selector<Channel<?>> selector;

        private final long pollTime = 1000L; //TODO: read in via properties or self-tune

        private Worker(final ThreadFactory threadFactory,
                       final int validOps) {
            this.thread = threadFactory.newThread(this);
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            this.selector = new ChannelSelector(validOps);
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
                      if(selector.isReady()) selector.poll(pollTime, TimeUnit.MILLISECONDS);
                      else sleep(pollTime, TimeUnit.MILLISECONDS); //TODO: add signalling/wakeup from selectors...
                  }
              }
              finally {
                  destroySignal.countDown();
              }
        }

        public void register(final Handler<? extends Channel<?>> handler, final int ops) {
            selector.register(handler, ops);
        }

        public void deregister(final Handler<? extends Channel<?>> handler, final int ops) {
            selector.deregister(handler, ops);
        }

        public void destroy() {
            try {
                if(destroySignal.getCount() != 0) {
                    try {
                        try {
                            selector.destroy();
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


        private void sleep(final long time, final TimeUnit timeUnit) {
            try {
                Thread.sleep(timeUnit.toMillis(time));
            }
            catch(final InterruptedException e) {
                swallow(e);
            }
        }
    }

    //TODO: externalise
    private static final class ChannelSelector implements Selector<Channel<?>> {

        private final int validOps;
        private final Lock lock;

        private volatile Handler<? extends Channel<?>>[] readHandlers;
        private volatile Handler<? extends Channel<?>>[] writeHandlers;

        public ChannelSelector(final int validOps) {
            this.readHandlers = Arrays.newArray(Handler.class, 0);
            this.writeHandlers = Arrays.newArray(Handler.class, 0);
            this.lock = new ReentrantLock();
            this.validOps = validOps;
        }

        public boolean isReady() {
            return (writeHandlers.length != 0 || readHandlers.length != 0);
        }

        public void poll(final long time, final TimeUnit timeUnit) {
            doWrite();
            doRead();
        }

        public void register(final Handler<? extends Channel<?>> handler, final int ops) {
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

        public void deregister(final Handler<? extends Channel<?>> handler, final int ops) {
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
}
