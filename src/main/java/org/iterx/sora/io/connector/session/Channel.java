package org.iterx.sora.io.connector.session;

public interface Channel<T> {

    void open();

    void read(T value);

    void write(T value);

    void flush();

    void close();

    //TODO: add destroy();   

    public interface Callback<C extends Channel<T>, T> {

        void onOpen(C channel);

        void onClose(C channel);

        void onRead(C channel, T value);

        void onWrite(C channel, T value);

        void onAbort(C channel, Throwable throwable);
    }
}

