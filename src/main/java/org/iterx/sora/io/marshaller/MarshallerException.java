package org.iterx.sora.io.marshaller;

import org.iterx.sora.io.IoException;

public class MarshallerException extends IoException {

    public MarshallerException() {
    }

    public MarshallerException(final String message) {
        super(message);
    }

    public MarshallerException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

    public MarshallerException(final Throwable throwable) {
        super(throwable);
    }
}
