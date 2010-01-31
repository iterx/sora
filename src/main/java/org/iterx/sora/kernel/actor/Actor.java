package org.iterx.sora.kernel.actor;

import org.iterx.sora.collection.queue.MultiProducerMultiConsumerBlockingQueue;
import org.iterx.sora.collection.queue.MultiProducerSingleConsumerBlockingQueue;
import org.iterx.sora.kernel.actor.receiver.Receiver;
import org.iterx.sora.kernel.actor.sender.Sender;
import org.iterx.sora.kernel.thread.KernelThread;
import org.iterx.sora.kernel.thread.KernelThreadFactory;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Actor<T> implements Sender<T> {

    private final Receiver<? super T> receiver;
    private final BlockingQueue<T> blockingQueue;
    private final KernelThreadFactory kernelThreadPool;
    private final Actor.Worker[] workers;
    private final Lock stateLock;

    private volatile State state;

    public Actor(final KernelThreadFactory kernelThreadPool,
                 final Receiver<? super T> receiver,
                 final int capacity) {
        this(kernelThreadPool, receiver, capacity, 1);
    }

    //TODO: pass in error handling callbacks...
    public Actor(final KernelThreadFactory kernelThreadPool,
                 final Receiver<? super T> receiver,
                 final int capacity,
                 final int workers) {
        this.stateLock = new ReentrantLock();
        this.kernelThreadPool = kernelThreadPool;
        this.receiver = receiver;
        this.blockingQueue = (workers == 1)?
                             new MultiProducerSingleConsumerBlockingQueue<T>(capacity) :
                             new MultiProducerMultiConsumerBlockingQueue<T>(capacity);
        this.workers = new Actor.Worker[workers];
        this.state = State.STOPPED;
    }

    public void start() {
        changeState(State.STARTING);
    }

    public void send(final T object) {
        try {
            blockingQueue.put(object);
        }
        catch(final Throwable throwable) {
            throw rethrow(throwable);
        }
    }

    public void flush() {
        while(!blockingQueue.isEmpty() && !Thread.currentThread().isInterrupted()) {
            Thread.yield();
        }
    }

    public void stop() {
        changeState(State.STOPPING);
    }

    public void destroy() {
        changeState(State.DESTROYING);
    }

    private void changeState(final State newState)
    {
        stateLock.lock();
        try {
            if(state.allowState(newState)) {
                for(State nextState = newState; state != nextState; nextState = state.run(this)) state = nextState;
            }
        }
        finally {
            stateLock.unlock();
        }
    }

    private void createWorkers() {
        for(int i = workers.length; i-- != 0;) if(workers[i] == null) workers[i] = new Worker(kernelThreadPool);
    }

    private void destroyWorkers() {
        for(int i = workers.length; i-- != 0;) if(workers[i] != null) workers[i].destroy();
    }

    private void onStop(final Worker worker) {
        for(int i = workers.length; i-- != 0;) if(workers[i] == worker) workers[i] = null;
    }

    private void onAbort(final Worker worker) {
        for(int i = workers.length; i-- != 0;) if(workers[i] == worker) workers[i] = null;
        changeState(State.ABORTING);
    }

    private enum State {
        STOPPED {
            @Override
            public boolean allowState(final State newState) {
                return newState == STARTING || super.isAllowed(newState);
            }
        },
        STARTING {
            @Override
            public boolean allowState(final State newState) {
                return newState == STARTED || super.isAllowed(newState);
            }

            @Override
            public State run(final Actor<?> actor) {
                try {
                    actor.createWorkers();
                    return State.STARTED;
                }
                catch(final Throwable throwable) {
                    swallow(throwable);
                    return State.ABORTING;
                }
            }
        },
        STARTED {
            @Override
            public boolean allowState(final State newState) {
                return newState == STOPPING || super.isAllowed(newState);
            }
        },
        STOPPING {
            @Override
            public boolean allowState(final State newState) {
                return newState == STOPPED || super.isAllowed(newState);
            }

            @Override
            public State run(final Actor<?> actor) {
                try {
                    actor.destroyWorkers();
                    return State.STOPPED;
                }
                catch(final Throwable throwable) {
                    swallow(throwable);
                    return State.ABORTING;
                }
            }
        },
        ABORTING {
            @Override
            public boolean allowState(final State newState) {
                return newState == State.STOPPING || newState == State.DESTROYING;
            }
        },
        DESTROYING {
            @Override
            public boolean allowState(final State newState) {
                return false;
            }

            @Override
            public State run(final Actor<?> actor) {
                try {
                    actor.destroyWorkers();
                }
                catch(final Throwable throwable) {
                    swallow(throwable);
                }
                return this;
            }
        };

        public State run(final Actor<?> actor) {
            return this;
        }

        public boolean allowState(final State newState) {
            if(isSame(newState)) return false;
            else if(isAllowed(newState)) return true;
            throw new IllegalStateException("Invalid state transition from '" + name() + "' to '" + newState.name() + "'");
        }

        protected boolean isAllowed(final State newState) {
            return (newState == State.ABORTING || newState == State.DESTROYING);
        }

        protected boolean isSame(final State newState) {
            return (this == newState);
        }
    }

    private class Worker implements Runnable {
        private final KernelThread kernelThread;
        private final CountDownLatch startSignal;
        private final CountDownLatch destroySignal;

        private Worker(final KernelThreadFactory kernelThreadPool) {
            this.kernelThread = kernelThreadPool.newThread(this);
            this.startSignal = new CountDownLatch(1);
            this.destroySignal = new CountDownLatch(1);
            init();
        }

        private void init() {
            try {
                kernelThread.start();
                startSignal.await();
            }
            catch(final Exception e) {
                throw rethrow(e);
            }
        }

        public void run() {
            try {
                startSignal.countDown();
                try {
                    while(!Thread.currentThread().isInterrupted()) {
                        try {
                            for(T object = blockingQueue.take(); object != null; object = blockingQueue.poll()) {
                                receiver.receive(object);
                            }
                        }
                        catch(final InterruptedException e) {
                            break;
                        }
                        finally {
                            receiver.flush();
                        }
                    }
                    onStop(this);
                }
                catch(final Throwable throwable) {
                    onAbort(this);
                    throw rethrow(throwable);
                }
            }
            finally {
                destroySignal.countDown();
            }
        }

        private void destroy() {
            try {
                if(destroySignal.getCount() != 0) kernelThread.interrupt();
                destroySignal.await();
            }
            catch(final Exception e) {
                swallow(e);
            }
        }
    }
}
