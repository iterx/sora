package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.AbstractSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;


public final class TcpSession extends AbstractSession<TcpChannel, ByteBuffer, ByteBuffer> {

    private final TcpChannelProvider socketChannelProvider;
    private final SessionCallback<? super TcpSession> sessionCallback;

    public TcpSession(final Multiplexor<? super TcpChannel> multiplexor,
                      final SessionCallback<? super TcpSession> sessionCallback,
                      final AcceptorEndpoint acceptorEndpoint) {
        this.socketChannelProvider = new AcceptorTcpChannelProvider(multiplexor, acceptorEndpoint);
        this.sessionCallback = sessionCallback;

    }

    public TcpSession(final Multiplexor<? super TcpChannel> multiplexor,
                      final SessionCallback<? super TcpSession> sessionCallback,
                      final ConnectorEndpoint connectorEndpoint) {
        this.socketChannelProvider = new ConnectorTcpChannelProvider(multiplexor, connectorEndpoint);
        this.sessionCallback = sessionCallback;

    }

    public TcpChannel newChannel(final Channel.ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback) {
        assertState(State.OPENED);
        return socketChannelProvider.newChannel(channelCallback);
    }

    @Override
    protected State onOpening() {
        socketChannelProvider.open();
        return super.onOpening();
    }

    @Override
    protected State onOpen() {
        sessionCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onClosing() {
        socketChannelProvider.close();
        return super.onClosing();
    }

    @Override
    protected State onClose() {
        sessionCallback.onClose(this);
        return super.onClose();
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        sessionCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    private void doAccept() {
        sessionCallback.onAccept(this);
    }

    private static abstract class TcpChannelProvider {

        public void open() {
        }

        abstract TcpChannel newChannel(Channel.ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback);

        public void close() {
        }

        protected SocketAddress toSocketAddress(final Uri uri) {
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        }
    }

    private final class ConnectorTcpChannelProvider extends TcpChannelProvider {

        private final Multiplexor<? super TcpChannel> multiplexor;
        private final SocketAddress socketAddress;

        private ConnectorTcpChannelProvider(final Multiplexor<? super TcpChannel> multiplexor, final ConnectorEndpoint connectorEndpoint){
            this.socketAddress = toSocketAddress(connectorEndpoint.getUri());
            this.multiplexor = multiplexor;
        }

        public TcpChannel newChannel(final Channel.ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback) {
            final SocketChannel socketChannel = newSocketChannel();
            return new ConnectorTcpChannel(multiplexor, channelCallback, socketChannel, socketAddress);
        }

        private SocketChannel newSocketChannel() {
            try {
                final SocketChannel socketChannel = SocketChannel.open();
                socketChannel.configureBlocking(false);
                socketChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                return socketChannel;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }
    }

    private final class AcceptorTcpChannelProvider extends TcpChannelProvider {

        private final AcceptorTcpChannel acceptorTcpChannel;

        private AcceptorTcpChannelProvider(final Multiplexor<? super TcpChannel> multiplexor,
                                           final AcceptorEndpoint acceptorEndpoint) {
            this.acceptorTcpChannel = new AcceptorTcpChannel(multiplexor,
                                                             new AcceptorTcpChannelCallback(),
                                                             newServerSocketChannel(),
                                                             toSocketAddress(acceptorEndpoint.getUri()));
        }

        @Override
        public void open() {
            acceptorTcpChannel.open();
        }

        @Override
        public void close() {
            acceptorTcpChannel.close();
        }

        public TcpChannel newChannel(final Channel.ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback) {
            return acceptorTcpChannel.accept(channelCallback);
        }

        private ServerSocketChannel newServerSocketChannel() {
            try {
                final ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                serverSocketChannel.configureBlocking(false);
                serverSocketChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                return serverSocketChannel;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        private class AcceptorTcpChannelCallback implements Channel.AcceptorChannelCallback<TcpChannel, ByteBuffer, ByteBuffer> {

            public void onAccept(final TcpChannel channel) {
                doAccept();
            }
        }
    }
}