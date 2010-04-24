package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

public interface TcpChannel extends Channel<ByteBuffer, ByteBuffer>, NioChannel<SelectableChannel> {}