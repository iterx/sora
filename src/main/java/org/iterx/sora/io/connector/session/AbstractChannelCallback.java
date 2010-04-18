package org.iterx.sora.io.connector.session;


public abstract class AbstractChannelCallback<C extends Channel<R, W>, R, W> implements Channel.ChannelCallback<C, R, W> {

    public void onOpen(final C channel) {
    }

    public void onClose(final C channel) {
    }

    public void onRead(final C channel, final R value) {
    }

    public void onWrite(final C channel, final W value) {
    }

    public void onAbort(final C channel, final Throwable throwable) {
    }
}
