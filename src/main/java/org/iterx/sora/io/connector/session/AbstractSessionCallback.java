package org.iterx.sora.io.connector.session;


public abstract class AbstractSessionCallback<S extends Session<?, ?, ?>> implements Session.SessionCallback<S> {

    public void onOpen(final S session) {
    }

    public void onAccept(final S session) {
    }

    public void onClose(final S session) {
    }

    public void onAbort(final S session, final Throwable throwable) {
    }
}
