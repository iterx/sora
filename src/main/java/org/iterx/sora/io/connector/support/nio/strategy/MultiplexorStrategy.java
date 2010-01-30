package org.iterx.sora.io.connector.support.nio.strategy;

import java.nio.channels.Channel;

public interface MultiplexorStrategy<T extends Channel> {

    public static final int OPEN_OP = 0x1;
    public static final int READ_OP = 0x2;
    public static final int WRITE_OP = 0x4;
    public static final int CLOSE_OP = 0x8;

    void register(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

    void deregister(MultiplexorHandler<? extends T> multiplexorHandler, int ops);

    void destroy();

    public interface MultiplexorHandler<T extends Channel> {

        T getChannel();

        void doOpen();

        int doRead(int length);

        int doWrite(int length);

        void doClose();
    }
}
