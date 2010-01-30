package org.iterx.sora.realtime.thread;


public abstract class RealtimeThread extends Thread {

    protected RealtimeThread(final ThreadGroup threadGroup, final String name) {
        super(threadGroup, name);
    }

    public abstract void run();

    public static RealtimeThread currentThread() {
        try {
            return (RealtimeThread) Thread.currentThread();
        }
        catch(final ClassCastException e) {
            throw new IllegalStateException();
        }
    }
}
