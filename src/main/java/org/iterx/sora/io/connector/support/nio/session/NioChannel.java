package org.iterx.sora.io.connector.support.nio.session;

import org.iterx.sora.io.connector.session.Channel;
import java.nio.ByteBuffer;

public interface NioChannel<C extends java.nio.channels.Channel> extends Channel<ByteBuffer, ByteBuffer> {

    C getChannel();
}
