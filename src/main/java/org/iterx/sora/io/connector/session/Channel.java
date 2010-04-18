package org.iterx.sora.io.connector.session;

public interface Channel<R, W> {

    @SuppressWarnings("unchecked")
    public static ChannelCallback<Channel<?, ?>, ?, ?> NO_OP_CHANNEL_CALLBACK = new AbstractChannelCallback(){};

    void open();

    void read(R value);

    void write(W value);

    void flush();

    void close();

    //TODO: add destroy();   

    public interface ChannelCallback<C extends Channel<R, W>, R, W> {

        void onOpen(C channel);

        void onRead(C channel, R value);

        void onWrite(C channel, W value);

        void onAbort(C channel, Throwable throwable);

        void onClose(C channel);
    }
}

