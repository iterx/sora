package org.iterx.sora.io.connector.multiplexor;

import org.iterx.sora.io.connector.multiplexor.selector.Selector;
import org.iterx.sora.io.connector.multiplexor.selector.SelectorFactory;
import org.iterx.sora.io.connector.session.Channel;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class PoolingMultiplexor <T extends Channel<ByteBuffer, ByteBuffer>> implements Multiplexor<T> {

    private final Worker<T> openCloseWorker;
    private final Worker<T> writeWorker;
    private final Worker<T> readWorker;

    private PoolingMultiplexor(final Worker<T> openCloseWorker,
                               final Worker<T> writeWorker,
                               final Worker<T> readWorker) {
        this.openCloseWorker = openCloseWorker;
        this.writeWorker = writeWorker;
        this.readWorker = readWorker;
    }

    public static <T extends Channel<ByteBuffer, ByteBuffer>> PoolingMultiplexor<T> newSinglePoolingMultiplexor(final ThreadFactory threadFactory,
                                                                                           final SelectorFactory<T> selectorFactory) {
        final Worker<T> readWriteOpenCloseWorker = new Worker<T>(threadFactory, selectorFactory.newSelector(), READ_OP|WRITE_OP|OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor<T>(readWriteOpenCloseWorker, readWriteOpenCloseWorker, readWriteOpenCloseWorker);
    }

    public static <T extends Channel<ByteBuffer, ByteBuffer>> PoolingMultiplexor<T> newOpenReadWritePoolingMultiplexor(final ThreadFactory threadFactory,
                                                                                                  final SelectorFactory<T> selectorFactory) {
        final Worker<T> readWorker = new Worker<T>(threadFactory, selectorFactory.newSelector(), READ_OP);
        final Worker<T> writeWorker = new Worker<T>(threadFactory, selectorFactory.newSelector(), WRITE_OP);
        final Worker<T> openCloseWorker = new Worker<T>(threadFactory, selectorFactory.newSelector(), OPEN_OP|CLOSE_OP);
        return new PoolingMultiplexor<T>(openCloseWorker, writeWorker, readWorker);
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

    private static final class Worker<T extends Channel<ByteBuffer, ByteBuffer>> implements Runnable {

        private final Thread thread;
        private final CountDownLatch startSignal;
        private final CountDownLatch destroySignal;
        private final Lock wakeupLock;
        private final Condition wakeupCondition;

        private final Selector<T> selector;
        private final int validOps;

        private final long pollTime = 1000L; //TODO: read in via properties or self-tune

        private Worker(final ThreadFactory threadFactory,
                       final Selector<T> selector,
                       final int validOps) {
            this.thread = threadFactory.newThread(this);
            this.wakeupLock = new ReentrantLock();
            this.wakeupCondition = wakeupLock.newCondition();
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            this.selector = selector;
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
                    if(selector.isReady()) {
                        if(selector.poll(pollTime, TimeUnit.MILLISECONDS)) continue;
                    }
                    sleep(pollTime, TimeUnit.MILLISECONDS);
                }
              }
              finally {
                  destroySignal.countDown();
              }
        }

        public void register(final Handler<? extends T> multiplexorHandler, final int ops) {
            if((ops & validOps) != 0) {
                if(selector.register(multiplexorHandler, ops & validOps)) wakeup();
            }
        }

        public void deregister(final Handler<? extends T> multiplexorHandler, final int ops) {
            if((ops & validOps) != 0) {
                selector.deregister(multiplexorHandler, ops & validOps);
            }
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
