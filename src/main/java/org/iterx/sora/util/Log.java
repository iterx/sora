package org.iterx.sora.util;

public final class Log {

    public void fatal(final Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    public void error(final Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    public void warn(final Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    public void info(final Throwable throwable) {
        throwable.printStackTrace(System.err);
    }

    public void debug(final Throwable throwable) {
        throwable.printStackTrace(System.err);
    }
}
