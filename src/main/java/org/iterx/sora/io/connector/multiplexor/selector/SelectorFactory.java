package org.iterx.sora.io.connector.multiplexor.selector;

import org.iterx.sora.io.connector.session.Channel;

import java.nio.ByteBuffer;

public interface SelectorFactory<T extends Channel<ByteBuffer, ByteBuffer>> {

    Selector<T> newSelector();
}
