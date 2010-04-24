package org.iterx.sora.io.connector.support.nio.session;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.session.Channel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface NioChannel<C extends java.nio.channels.Channel> extends Channel<ByteBuffer, ByteBuffer> {

    C getChannel();

    public static final class Helper {

        public static int read(final ReadableByteChannel channel, final ByteBuffer buffer) {
            try {
                return channel.read(buffer);
            }
            catch(final ClosedChannelException e) {
                return -1;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public static int write(final WritableByteChannel channel, final ByteBuffer buffer) {
            try {
                return channel.write(buffer);
            }
            catch(final ClosedChannelException e) {
                return -1;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }
    }
}
