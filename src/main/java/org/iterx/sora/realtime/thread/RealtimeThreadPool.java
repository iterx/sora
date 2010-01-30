package org.iterx.sora.realtime.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public final class RealtimeThreadPool implements ThreadFactory {

    private final RealtimeThreadGroup realtimeThreadGroup;
    private final Semaphore realtimeThreadCount;

    public RealtimeThreadPool(final String name) {
        this(name, Integer.MAX_VALUE);
    }

    public RealtimeThreadPool(final String name, final int capacity) {
        this.realtimeThreadGroup = new RealtimeThreadGroup(name);
        this.realtimeThreadCount = new Semaphore(capacity);
    }

    public RealtimeThread newThread(final Runnable runnable) {
        if(runnable == null) throw new IllegalArgumentException("runnable == null");
        try {
            realtimeThreadCount.acquire();
            return new RealtimeThread(runnable);
        }
        catch(final Throwable throwable){
            throw rethrow(throwable);
        }
    }

     public void destroy() {
        if(!realtimeThreadGroup.isDestroyed()) {
            while(realtimeThreadGroup.activeCount() != 0) {
                realtimeThreadGroup.interrupt();
                Thread.yield();
            }
            realtimeThreadGroup.destroy();
            while(realtimeThreadCount.availablePermits() != 0) {
                realtimeThreadCount.drainPermits();
                Thread.yield();
            }
        }
    }
    
    private final class RealtimeThread extends org.iterx.sora.realtime.thread.RealtimeThread  {

        private final Runnable runnable;

        private RealtimeThread(final Runnable runnable) {
            super(realtimeThreadGroup, realtimeThreadGroup.newThreadName());
            this.runnable = runnable;
        }

        @Override
        public void run() {
            try {
                //open stack/heap allocators
                runnable.run();
            }
            finally {
                //release stack/heap allocators
                realtimeThreadCount.release();
            }
        }
    }

    private final class RealtimeThreadGroup extends ThreadGroup {

        private final AtomicLong threadIdSequence = new AtomicLong(0);

        public RealtimeThreadGroup(final String name) {
            super(name);
        }

        public String newThreadName() {
            return getName() + "-" + threadIdSequence.incrementAndGet();
        }
    }

    private static RuntimeException rethrow(final Throwable throwable)  {
        if(throwable instanceof Error) throw (Error) throwable;
        return (throwable instanceof RuntimeException)? (RuntimeException) throwable : new RuntimeException(throwable);
    }
}
