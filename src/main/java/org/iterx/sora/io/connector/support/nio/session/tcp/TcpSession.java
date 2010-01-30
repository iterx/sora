package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.AbstractSession;
import org.iterx.sora.io.connector.support.nio.strategy.MultiplexorStrategy;
import org.iterx.sora.util.collection.queue.MultiProducerSingleConsumerBlockingQueue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.iterx.sora.util.Exception.swallow;

public final class TcpSession extends AbstractSession<TcpChannel> {

    private final TcpChannelProvider socketChannelProvider;
    private final Callback<? super TcpSession> sessionCallback;
    private final MultiplexorStrategy<? super SelectableChannel> multiplexorStrategy;

    public TcpSession(final MultiplexorStrategy<? super SelectableChannel> multiplexorStrategy,
                      final Callback<? super TcpSession> sessionCallback,
                      final AcceptorEndpoint acceptorEndpoint) {
        this.socketChannelProvider = new AcceptorTcpChannelProvider(acceptorEndpoint);
        this.multiplexorStrategy = multiplexorStrategy;
        this.sessionCallback = sessionCallback;
    }

    public TcpSession(final MultiplexorStrategy<? super SelectableChannel> multiplexorStrategy,
                      final Callback<? super TcpSession> sessionCallback,
                      final ConnectorEndpoint connectorEndpoint) {
        this.socketChannelProvider = new ConnectorTcpChannelProvider(connectorEndpoint);
        this.multiplexorStrategy = multiplexorStrategy;
        this.sessionCallback = sessionCallback;
    }


    public TcpChannel newChannel(final Channel.Callback<? super TcpChannel, ByteBuffer> channelCallback) {
        assertState(State.OPEN);
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
    protected State onClosed() {
        sessionCallback.onClose(this);
        return super.onClosed();
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

        abstract TcpChannel newChannel(Channel.Callback<? super TcpChannel, ByteBuffer> channelCallback);

        public void close() {
        }

        protected SocketAddress toSocketAddress(final Uri uri) {
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        }
    }

    private final class ConnectorTcpChannelProvider extends TcpChannelProvider {

        private final SocketAddress socketAddress;

        private ConnectorTcpChannelProvider(final ConnectorEndpoint connectorEndpoint){
            this.socketAddress = toSocketAddress(connectorEndpoint.getUri());
        }

        public TcpChannel newChannel(final Channel.Callback<? super TcpChannel, ByteBuffer> channelCallback) {
            final SocketChannel socketChannel = newSocketChannel();
            return new TcpChannel(multiplexorStrategy, channelCallback, socketChannel, socketAddress);
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

        private final MultiProducerSingleConsumerBlockingQueue<SocketChannel> acceptBlockingQueue;
        private final ServerSocketChannel serverSocketChannel;
        private final SocketAddress socketAddress;
        private final MultiplexorHandler multiplexorHandler;

        private AcceptorTcpChannelProvider(final AcceptorEndpoint acceptorEndpoint) {
            this.acceptBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<SocketChannel>(32);
            this.multiplexorHandler = new MultiplexorHandler();
            this.socketAddress = toSocketAddress(acceptorEndpoint.getUri());
            this.serverSocketChannel = newServerSocketChannel();
        }

        @Override
        public void open() {
            try {
                serverSocketChannel.bind(socketAddress);
                multiplexorStrategy.register(multiplexorHandler, MultiplexorStrategy.OPEN_OP|MultiplexorStrategy.CLOSE_OP);
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        public TcpChannel newChannel(final Channel.Callback<? super TcpChannel, ByteBuffer> channelCallback) {
            try {
                final SocketChannel socketChannel = acceptBlockingQueue.poll();
                if(socketChannel != null) {
                    socketChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                    return new TcpChannel(multiplexorStrategy, channelCallback, socketChannel, socketChannel.getRemoteAddress());
                }
                return null;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        @Override
        public void close() {
            try {
                multiplexorStrategy.deregister(multiplexorHandler, MultiplexorStrategy.OPEN_OP|MultiplexorStrategy.CLOSE_OP);
                serverSocketChannel.close();
                for(SocketChannel socketChannel = acceptBlockingQueue.poll();
                    socketChannel != null;
                    socketChannel = acceptBlockingQueue.poll()) socketChannel.close();
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
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

        private class MultiplexorHandler implements MultiplexorStrategy.MultiplexorHandler<ServerSocketChannel> {

            public ServerSocketChannel getChannel() {
                return serverSocketChannel;
            }

            public void doOpen() {
                try {
                    for(SocketChannel socketChannel = serverSocketChannel.accept();
                        socketChannel != null;
                        socketChannel = serverSocketChannel.accept()) {
                        socketChannel.configureBlocking(false);
                        acceptBlockingQueue.add(socketChannel);
                        doAccept();
                    }
                }
                catch(final Throwable throwable) {
                    changeState(State.ABORTING, throwable);
                    swallow(throwable);
                }
            }

            public int doRead(final int length) {
                throw new UnsupportedOperationException();
            }

            public int doWrite(final int length) {
                throw new UnsupportedOperationException();
            }

            public void doClose() {
                changeState(State.CLOSING);
            }
        }
    }
}