package org.iterx.sora.kernel.thread;


public abstract class KernelThread extends Thread {

    protected KernelThread(final ThreadGroup threadGroup, final String name) {
        super(threadGroup, name);
    }

    public abstract void run();

    public static KernelThread currentThread() {
        try {
            return (KernelThread) Thread.currentThread();
        }
        catch(final ClassCastException e) {
            throw new IllegalStateException();
        }
    }
}
