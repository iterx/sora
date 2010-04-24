package org.iterx.sora.io.connector.session;


public interface Session<C extends Channel<R, W>, R, W> {

    @SuppressWarnings("unchecked")
    public static SessionCallback<Session<?, ?, ?>> NO_OP_SESSION_CALLBACK = new AbstractSessionCallback(){};

    //TODO: add void setOption() & destroy();

    Session<C, R, W> open();

    C newChannel(Channel.ChannelCallback<? super C, R, W> channelCallback);

    Session<C, R, W> close();

    public interface SessionCallback<S extends Session> {

        void onOpen(S session);

        void onAccept(S session);

        void onAbort(S session, Throwable throwable);

        void onClose(S session);
    }
}
