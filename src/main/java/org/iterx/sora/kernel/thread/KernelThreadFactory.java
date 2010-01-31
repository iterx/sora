package org.iterx.sora.kernel.thread;

import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

import static org.iterx.sora.util.Exception.rethrow;

public final class KernelThreadFactory implements ThreadFactory {

    private final KernelThreadGroup kernelThreadGroup;
    private final Semaphore kernelThreadCount;

    public KernelThreadFactory(final String name) {
        this(name, Integer.MAX_VALUE);
    }

    public KernelThreadFactory(final String name, final int capacity) {
        this.kernelThreadGroup = new KernelThreadGroup(name);
        this.kernelThreadCount = new Semaphore(capacity);
    }

    public KernelThread newThread(final Runnable runnable) {
        if(runnable == null) throw new IllegalArgumentException("runnable == null");
        try {
            kernelThreadCount.acquire();
            return new KernelThread(runnable);
        }
        catch(final Throwable throwable){
            throw rethrow(throwable);
        }
    }

     public void destroy() {
        if(!kernelThreadGroup.isDestroyed()) {
            while(kernelThreadGroup.activeCount() != 0) {
                kernelThreadGroup.interrupt();
                Thread.yield();
            }
            kernelThreadGroup.destroy();
            while(kernelThreadCount.availablePermits() != 0) {
                kernelThreadCount.drainPermits();
                Thread.yield();
            }
        }
    }
    
    private final class KernelThread extends org.iterx.sora.kernel.thread.KernelThread {

        private final Runnable runnable;

        private KernelThread(final Runnable runnable) {
            super(kernelThreadGroup, kernelThreadGroup.newThreadName());
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
                kernelThreadCount.release();
            }
        }
    }

    private final class KernelThreadGroup extends ThreadGroup {

        private final AtomicLong threadIdSequence = new AtomicLong(0);

        public KernelThreadGroup(final String name) {
            super(name);
        }

        public String newThreadName() {
            return getName() + "-" + threadIdSequence.incrementAndGet();
        }
    }
}
