package org.iterx.sora.io.connector.multiplexor;

import org.iterx.sora.io.connector.session.Channel;

import java.util.concurrent.TimeUnit;

public interface Multiplexor<T extends Channel> {

    public static final int OPEN_OP = 0x1;
    public static final int READ_OP = 0x2;
    public static final int WRITE_OP = 0x4;
    public static final int CLOSE_OP = 0x8;

    void register(Handler<? extends T> handler, int ops);

    void deregister(Handler<? extends T> handler, int ops);

    void destroy();

    public interface Handler<T extends Channel> {

        T getChannel();

        void doOpen();

        int doRead(int length);

        int doWrite(int length);

        void doClose();
    }

    public interface Selector<T extends Channel> {

        boolean isReady();

        void poll(long time, TimeUnit timeUnit);

        void register(Handler<? extends T> handler, int ops);

        void deregister(Handler<? extends T> handler, int ops);

        void destroy();
    }
}
