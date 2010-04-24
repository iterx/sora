package org.iterx.sora.io.connector.session.vm;

import org.iterx.sora.collection.queue.MultiProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractSession;
import org.iterx.sora.io.connector.session.Channel;

import java.nio.ByteBuffer;

//TODO: maps/multiplexes channels endpoints.
public final class VmSession extends AbstractSession<VmChannel, ByteBuffer, ByteBuffer> {

    private final VmChannelProvider socketChannelProvider;
    private final SessionCallback<? super VmSession> sessionCallback;
    private final Multiplexor<? super Channel<ByteBuffer, ByteBuffer>> multiplexor;

    public VmSession(final Multiplexor<? super Channel<ByteBuffer, ByteBuffer>> multiplexor,
                     final SessionCallback<? super VmSession> sessionCallback,
                     final AcceptorEndpoint acceptorEndpoint) {
        this.socketChannelProvider = new AcceptorVmChannelProvider(acceptorEndpoint);
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
    }

    public VmSession(final Multiplexor<? super Channel<ByteBuffer, ByteBuffer>> multiplexor,
                     final SessionCallback<? super VmSession> sessionCallback,
                     final ConnectorEndpoint connectorEndpoint) {
        this.socketChannelProvider = new ConnectorVmChannelProvider(connectorEndpoint);
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
    }

    public VmChannel newChannel(final Channel.ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback) {
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

    private static abstract class VmChannelProvider {

        public void open() {
        }

        abstract VmChannel newChannel(Channel.ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback);

        public void close() {
        }
    }

    //TODO: looks up acceptor VmChannel -> each connector holds queues!!! -> acceptor is just multiplexor???
    private final class ConnectorVmChannelProvider extends VmChannelProvider {

        private ConnectorVmChannelProvider(final ConnectorEndpoint connectorEndpoint){
        }

        public VmChannel newChannel(final Channel.ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback) {
/*
            final SocketChannel socketChannel = newSocketChannel();
            return new VmChannel(multiplexor, channelCallback, socketChannel, socketAddress);
*/
            //TODO: proxy until open!!!
            return new VmChannel(multiplexor, channelCallback, null);
        }

    }

    private final class AcceptorVmChannelProvider extends VmChannelProvider {

        private final MultiProducerSingleConsumerBlockingQueue<VmChannel> acceptBlockingQueue;
        private final AcceptorVmChannel acceptorVmChannel;

        private AcceptorVmChannelProvider(final AcceptorEndpoint acceptorEndpoint) {
            this.acceptBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<VmChannel>(32);
            this.acceptorVmChannel = new AcceptorVmChannel();
        }

        @Override
        public void open() {
            acceptorVmChannel.open();
        }

        @Override
        public void close() {
            acceptorVmChannel.close();
        }

        public VmChannel newChannel(final Channel.ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback) {
            return null;
        }


        private class AcceptorVmChannel implements Channel<ByteBuffer, ByteBuffer> {

            private final MultiplexorHandler multiplexorHandler;

            private AcceptorVmChannel() {
                this.multiplexorHandler = new MultiplexorHandler();
            }

            @Override
            public Channel<ByteBuffer, ByteBuffer> open() {
                multiplexor.register(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
                return this;
            }

            public Channel<ByteBuffer, ByteBuffer> read(final ByteBuffer value) {
                throw new UnsupportedOperationException();
            }

            public Channel<ByteBuffer, ByteBuffer> write(final ByteBuffer value) {
                throw new UnsupportedOperationException();
            }

            public Channel<ByteBuffer, ByteBuffer> flush() {
                throw new UnsupportedOperationException();
            }

            @Override
            public Channel<ByteBuffer, ByteBuffer> close() {
                multiplexor.deregister(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
                for(VmChannel vmChannel = acceptBlockingQueue.poll();
                    vmChannel != null;
                    vmChannel = acceptBlockingQueue.poll()) vmChannel.close();
                return this;
            }

            private class MultiplexorHandler implements Multiplexor.Handler<AcceptorVmChannel> {

                public AcceptorVmChannel getChannel() {
                    return AcceptorVmChannel.this;
                }

                public void doOpen() {
/*
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
                        changeState(State.ABORTED, throwable);
                        swallow(throwable);
                    }
*/
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