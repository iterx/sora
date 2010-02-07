package org.iterx.sora.io.connector.multiplexor;

import org.iterx.sora.io.connector.multiplexor.selector.Selector;
import org.iterx.sora.io.connector.multiplexor.selector.SelectorFactory;
import org.iterx.sora.io.connector.session.Channel;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
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

    public static PoolingMultiplexor<?> newSinglePoolingMultiplexor(final ThreadFactory threadFactory,
                                                                    final SelectorFactory<? extends Channel<?>> selectorFactory) {
        final Worker readWriteOpenCloseWorker = new Worker(threadFactory, selectorFactory.newSelector(), READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static PoolingMultiplexor<?> newOpenReadWritePoolingMultiplexor(final ThreadFactory threadFactory,
                                                                           final SelectorFactory<? extends Channel<?>> selectorFactory) {
        //TODO: need individual selector instances per worker...
        final Worker readWorker = new Worker(threadFactory, selectorFactory.newSelector(), READ_OP);
        final Worker writeWorker = new Worker(threadFactory, selectorFactory.newSelector(), WRITE_OP);
        final Worker openCloseWorker = new Worker(threadFactory, selectorFactory.newSelector(), OPEN_OP|CLOSE_OP);
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
        private final Lock wakeupLock;
        private final Condition wakeupCondition;

        private final Selector<? extends Channel<?>>[] selectors;
        private final int validOps;

        private final long pollTime = 1000L; //TODO: read in via properties or self-tune

        private Worker(final ThreadFactory threadFactory,
                       final Selector<? extends Channel<?>> selector,
                       final int validOps) {
            this.thread = threadFactory.newThread(this);
            this.wakeupLock = new ReentrantLock();
            this.wakeupCondition = wakeupLock.newCondition();
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            this.selectors = new Selector[] { selector };
            this.validOps = validOps;
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
                    boolean busy = false;
                    for(final Selector<? extends Channel<?>> selector : selectors)
                    {
                        if(selector.isReady()) busy |= selector.poll(pollTime, TimeUnit.MILLISECONDS);
                    }
                    if(!busy) sleep(pollTime, TimeUnit.MILLISECONDS);
                }
              }
              finally {
                  destroySignal.countDown();
              }
        }

        public void register(final Handler<? extends Channel<?>> handler, final int ops) {
            if((ops & validOps) != 0) {
                if(getSelector(handler).register(handler, ops & validOps)) wakeup();
            }
        }

        public void deregister(final Handler<? extends Channel<?>> handler, final int ops) {
            if((ops & validOps) != 0) {
                getSelector(handler).deregister(handler, ops & validOps);
            }
        }

        public void destroy() {
            try {
                if(destroySignal.getCount() != 0) {
                    try {
                        try {
                            for(final Selector<? extends Channel<?>> selector : selectors) selector.destroy();
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
        private <T extends Channel> Selector<T> getSelector(final Handler<?> handler) {
            for(final Selector<? extends Channel<?>> selector : selectors) {
                if(selector.supports(handler)) return (Selector<T>) selector;
            }
            throw new UnsupportedOperationException();
        }

        private void sleep(final long time, final TimeUnit timeUnit) {
            wakeupLock.lock();
            try {
                wakeupCondition.await(time, timeUnit);
            }
            catch(final InterruptedException e) {
                swallow(e);
            }
            finally {
                wakeupLock.unlock();
            }
        }

        private void wakeup() {
            wakeupLock.lock();
            try {
                wakeupCondition.signalAll();
            }
            finally {
                wakeupLock.unlock();
            }
        }
    }
}
