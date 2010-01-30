package org.iterx.sora.util;

public final class Exception {

    private Exception() {
    }

    public static RuntimeException rethrow(final Throwable throwable)  {
        throwable.printStackTrace();
        if(throwable instanceof Error) throw (Error) throwable;
        return (throwable instanceof RuntimeException)? (RuntimeException) throwable : new RuntimeException(throwable);
    }

    public static RuntimeException rethrow(final Throwable throwable, final Log log)  {
        if(throwable instanceof Error) {
            log.fatal(throwable);
            throw (Error) throwable;
        }
        log.warn(throwable);
        return (throwable instanceof RuntimeException)? (RuntimeException) throwable : new RuntimeException(throwable);
    }

    public static void swallow(final Throwable throwable) {
        throwable.printStackTrace();
        if(throwable instanceof Error) throw (Error) throwable;
    }

    public static void swallow(final Throwable throwable, final Log log) {
        if(throwable instanceof Error) {
            log.fatal(throwable);
            throw (Error) throwable;
        }
        log.warn(throwable);
    }
}
