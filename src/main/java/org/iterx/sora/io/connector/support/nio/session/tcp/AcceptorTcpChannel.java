package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.collection.queue.MultiProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.io.connector.session.Channel;

import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static org.iterx.sora.util.Exception.swallow;

final class AcceptorTcpChannel extends AbstractChannel<ByteBuffer, ByteBuffer> implements TcpChannel {

    private final MultiProducerSingleConsumerBlockingQueue<SocketChannel> acceptBlockingQueue;
    private final ServerSocketChannel serverSocketChannel;
    private final SocketAddress socketAddress;
    private final AcceptorChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> acceptorChannelCallback;
    private final MultiplexorHandler multiplexorHandler;
    private final Multiplexor<? super TcpChannel> multiplexor;


    AcceptorTcpChannel(final Multiplexor<? super TcpChannel> multiplexor,
                       final AcceptorChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> acceptorChannelCallback,
                       final ServerSocketChannel serverSocketChannel,
                       final SocketAddress socketAddress) {
        this.acceptBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<SocketChannel>(32);
        this.multiplexorHandler = new MultiplexorHandler();

        this.multiplexor = multiplexor;
        this.acceptorChannelCallback = acceptorChannelCallback;
        this.serverSocketChannel = serverSocketChannel;
        this.socketAddress = socketAddress;
    }

    public ServerSocketChannel getChannel() {
        return serverSocketChannel;
    }

    public TcpChannel accept(final ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback) {
        assertState(State.OPEN);
        try {
            final SocketChannel socketChannel = acceptBlockingQueue.poll();
            if(socketChannel != null) {
                socketChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                return new ConnectorTcpChannel(multiplexor, channelCallback, socketChannel, socketChannel.getRemoteAddress());
            }
            return null;
        }
        catch(final IOException e) {
            throw new IoException(e);
        }
    }

    public Channel<ByteBuffer,ByteBuffer> read(final ByteBuffer value) {
        throw new UnsupportedOperationException();
    }

    public Channel<ByteBuffer, ByteBuffer> write(final ByteBuffer value) {
        throw new UnsupportedOperationException();
    }

    public Channel<ByteBuffer, ByteBuffer> flush() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected State onOpening() {
        try {
            serverSocketChannel.bind(socketAddress);
            multiplexor.register(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
            return super.onOpening();
        }
        catch(final IOException e) {
            throw new IoException(e);
        }
    }

    @Override
    protected State onClosing() {
        try {
            multiplexor.deregister(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
            serverSocketChannel.close();
            for(SocketChannel socketChannel = acceptBlockingQueue.poll();
                socketChannel != null;
                socketChannel = acceptBlockingQueue.poll()) socketChannel.close();
            return super.onClosing();
        }
        catch(final IOException e) {
            throw new IoException(e);
        }
    }

    private void doAccept() {
        acceptorChannelCallback.onAccept(this);
    }

    private class MultiplexorHandler implements Multiplexor.Handler<AcceptorTcpChannel> {

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
