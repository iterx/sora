package org.iterx.sora.util.clock;

import java.util.concurrent.TimeUnit;

public abstract class Clock {

    private static Clock clock = new DefaultClock();

    abstract protected long doCurrentTime(final TimeUnit timeUnit);

    public static long currentTime(final TimeUnit timeUnit) {
        return clock.doCurrentTime(timeUnit);
    }

    public static void setClock(final Clock clock) {
        assertClock(clock);
        Clock.clock = clock;
    }

    private static void assertClock(final Clock clock) {
        if(clock == null) throw new IllegalArgumentException("clock == null");
    }

    private static final class DefaultClock extends Clock {

        protected long doCurrentTime(final TimeUnit timeUnit) {
           return timeUnit.convert(System.currentTimeMillis(), timeUnit);
        }
    }
}
