package org.iterx.sora.io.connector.session;


public interface Session<C extends Channel<T>, T> {


    //TODO: add void setOption() & destroy();

    void open();

    C newChannel(Channel.Callback<? super C, T> channelCallback);

    void close();

    public interface Callback<S extends Session<?, ?>> {

        void onOpen(S session);

        void onAccept(S session);

        void onClose(S session);

        void onAbort(S session, Throwable throwable);
    }
}
