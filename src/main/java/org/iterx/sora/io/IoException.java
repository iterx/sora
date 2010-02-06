package org.iterx.sora.io;

public class IoException extends RuntimeException {

    public IoException() {
    }

    public IoException(final String message) {
        super(message);
    }

    public IoException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public IoException(final Throwable throwable) {
        super(throwable);
    }
}
