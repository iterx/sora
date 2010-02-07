package org.iterx.sora.io.connector.multiplexor.selector;

import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Channel;

import java.util.concurrent.TimeUnit;

public interface Selector<T extends Channel<?>> {

    boolean supports(Multiplexor.Handler<?> multiplexorHandler);

    boolean isReady();

    boolean poll(long time, TimeUnit timeUnit);

    boolean register(Multiplexor.Handler<? extends T> multiplexorHandler, int ops);

    boolean deregister(Multiplexor.Handler<? extends T> multiplexorHandler, int ops);

    void destroy();
}
