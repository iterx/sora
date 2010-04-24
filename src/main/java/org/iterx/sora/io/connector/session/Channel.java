package org.iterx.sora.io.connector.session;

public interface Channel<R, W> {

    @SuppressWarnings("unchecked")
    public static ChannelCallback<Channel<?, ?>, ?, ?> NO_OP_CHANNEL_CALLBACK = new AbstractChannelCallback(){};

    Channel<R, W> open();

    Channel<R, W> read(R value);

    Channel<R, W> write(W value);

    Channel<R, W> flush();

    Channel<R, W> close();

    //TODO: add destroy();   

    public interface ChannelCallback<C extends Channel<R, W>, R, W> { //TODO: Rename as ConnectorChannelCallback

        void onOpen(C channel);

        void onRead(C channel, R value);

        void onWrite(C channel, W value);

        void onAbort(C channel, Throwable throwable);

        void onClose(C channel);
    }

    public interface AcceptorChannelCallback<C extends Channel<R, W>, R, W> {

        void onAccept(C channel);
    }
}

