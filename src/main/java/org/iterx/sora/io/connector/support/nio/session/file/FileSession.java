package org.iterx.sora.io.connector.support.nio.session.file;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.AbstractSession;
import org.iterx.sora.collection.Collections;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.StandardOpenOption;

public final class FileSession extends AbstractSession<FileChannel, ByteBuffer, ByteBuffer> {

    private final FileChannelProvider fileChannelProvider;
    private final SessionCallback<? super FileSession> sessionCallback;
    private final Multiplexor<? super FileChannel> multiplexor;

    public FileSession(final Multiplexor<? super FileChannel> multiplexor,
                       final SessionCallback<? super FileSession> sessionCallback,
                       final ConnectorEndpoint connectorEndpoint) {
        this.fileChannelProvider = new ConnectorFileChannelProvider(connectorEndpoint);
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
    }

    public FileChannel newChannel(final Channel.ChannelCallback<? super FileChannel, ByteBuffer, ByteBuffer> channelCallback) {
        assertState(State.OPEN);
        return fileChannelProvider.newChannel(channelCallback);
    }

    @Override
    protected State onOpening() {
        fileChannelProvider.open();
        return super.onOpening();
    }

    @Override
    protected State onOpen() {
        sessionCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onClosing() {
        fileChannelProvider.close();
        return super.onClosing();
    }

    @Override
    protected State onClosed() {
        sessionCallback.onClose(this);
        return super.onClosed();
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        sessionCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    private static abstract class FileChannelProvider {

        public void open() {
        }

        abstract FileChannel newChannel(Channel.ChannelCallback<? super FileChannel, ByteBuffer, ByteBuffer> channelCallback);

        public void close() {
        }

        protected File toFile(final Uri uri) {
            return new File(uri.getPath());
        }
    }

    private final class ConnectorFileChannelProvider extends FileChannelProvider {

        private final File file;

        private ConnectorFileChannelProvider(final ConnectorEndpoint connectorEndpoint){
            this.file = toFile(connectorEndpoint.getUri());
        }

        public FileChannel newChannel(final Channel.ChannelCallback<? super FileChannel, ByteBuffer, ByteBuffer> channelCallback) {
            return new FileChannel(multiplexor, channelCallback, newFileChannel());
        }

        private java.nio.channels.FileChannel newFileChannel() {
            try {
                return java.nio.channels.FileChannel.open(file.toPath(),
                                                          Collections.newSet(StandardOpenOption.READ, StandardOpenOption.WRITE, StandardOpenOption.CREATE));
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }
    }
}