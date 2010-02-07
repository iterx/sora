package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.Multiplexor;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.AbstractSession;
import org.iterx.sora.collection.queue.MultiProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.iterx.sora.util.Exception.swallow;

public final class TcpSession extends AbstractSession<TcpChannel, ByteBuffer> {

    private final TcpChannelProvider socketChannelProvider;
    private final Callback<? super TcpSession> sessionCallback;
    private final Multiplexor<? super NioChannel> multiplexor;

    public TcpSession(final Multiplexor<? super NioChannel> multiplexor,
                      final Callback<? super TcpSession> sessionCallback,
                      final AcceptorEndpoint acceptorEndpoint) {
        this.socketChannelProvider = new AcceptorTcpChannelProvider(acceptorEndpoint);
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
    }

    public TcpSession(final Multiplexor<? super NioChannel> multiplexor,
                      final Callback<? super TcpSession> sessionCallback,
                      final ConnectorEndpoint connectorEndpoint) {
        this.socketChannelProvider = new ConnectorTcpChannelProvider(connectorEndpoint);
        this.multiplexor = multiplexor;
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
            return new TcpChannel(multiplexor, channelCallback, socketChannel, socketAddress);
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
        private final AcceptorTcpChannel acceptorTcpChannel;

        private AcceptorTcpChannelProvider(final AcceptorEndpoint acceptorEndpoint) {
            this.acceptBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<SocketChannel>(32);
            this.acceptorTcpChannel = new AcceptorTcpChannel(newServerSocketChannel(),
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

        public TcpChannel newChannel(final Channel.Callback<? super TcpChannel, ByteBuffer> channelCallback) {
            try {
                final SocketChannel socketChannel = acceptBlockingQueue.poll();
                if(socketChannel != null) {
                    socketChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                    return new TcpChannel(multiplexor, channelCallback, socketChannel, socketChannel.getRemoteAddress());
                }
                return null;
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

        private class AcceptorTcpChannel implements NioChannel<ServerSocketChannel> {

            private final ServerSocketChannel serverSocketChannel;
            private final SocketAddress socketAddress;
            private final Handler multiplexorHandler;

            private AcceptorTcpChannel(final ServerSocketChannel serverSocketChannel,
                                       final SocketAddress socketAddress) {
                this.multiplexorHandler = new Handler();
                this.serverSocketChannel = serverSocketChannel;
                this.socketAddress = socketAddress;

            }

            public ServerSocketChannel getChannel() {
                return serverSocketChannel;
            }

            @Override
            public void open() {
                try {
                    serverSocketChannel.bind(socketAddress);
                    multiplexor.register(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }
            public void read(final ByteBuffer value) {
                throw new UnsupportedOperationException();
            }

            public void write(final ByteBuffer value) {
                throw new UnsupportedOperationException();
            }

            public void flush() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
                try {
                    multiplexor.deregister(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
                    serverSocketChannel.close();
                    for(SocketChannel socketChannel = acceptBlockingQueue.poll();
                        socketChannel != null;
                        socketChannel = acceptBlockingQueue.poll()) socketChannel.close();
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }

            private class Handler implements Multiplexor.Handler<AcceptorTcpChannel> {

                public AcceptorTcpChannel getChannel() {
                    return AcceptorTcpChannel.this;
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
}