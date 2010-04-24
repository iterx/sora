package org.iterx.sora.util.clock;

import org.iterx.sora.kernel.thread.KernelThreadFactory;

import java.util.concurrent.TimeUnit;

import static org.iterx.sora.util.Exception.rethrow;


//TODO: add options to rewind time... -> need support for nano time
public final class SystemClock extends Clock {

    private volatile long timeInMillis;

    private SystemClock(final KernelThreadFactory kernelThreadFactory) {
        update();
        kernelThreadFactory.newThread(new Worker());
    }

    protected long doCurrentTime(final TimeUnit timeUnit) {
        return timeUnit.convert(timeInMillis, TimeUnit.MILLISECONDS);
    }

    private void update() {
        timeInMillis = System.currentTimeMillis();
    }

    private class Worker implements Runnable {

        public void run() {
            try {
                while(true) {
                    update();
                    Thread.sleep(1);         //TODO: should average over timescale...
                }
            }
            catch(final InterruptedException e) {
                throw rethrow(e);
            }
        }
    }
}
