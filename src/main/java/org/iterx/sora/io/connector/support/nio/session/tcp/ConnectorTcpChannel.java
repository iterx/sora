package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.collection.queue.SingleProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

final class ConnectorTcpChannel extends AbstractChannel<ByteBuffer, ByteBuffer> implements TcpChannel {

    private final Multiplexor<? super TcpChannel> multiplexor;
    private final ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback;
    private final SocketChannel socketChannel;
    private final SocketAddress socketAddress;
    private final MultiplexorHandler multiplexorHandler;

    private final BlockingQueue<ByteBuffer> readBlockingQueue;
    private final BlockingQueue<ByteBuffer> writeBlockingQueue;

    private final Lock queueLock;
    private final Condition emptyQueueCondition;

    private volatile int interestOps;

    ConnectorTcpChannel(final Multiplexor<? super TcpChannel> multiplexor,
                        final ChannelCallback<? super TcpChannel, ByteBuffer, ByteBuffer> channelCallback,
                        final SocketChannel socketChannel,
                        final SocketAddress socketAddress) {
        this.readBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.writeBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.queueLock = new ReentrantLock();
        this.emptyQueueCondition = queueLock.newCondition();
        this.multiplexorHandler = new MultiplexorHandler();

        this.multiplexor = multiplexor;
        this.channelCallback = channelCallback;
        this.socketChannel = socketChannel;
        this.socketAddress = socketAddress;
    }

    public SocketChannel getChannel() {
        return socketChannel;
    }

    public Channel<ByteBuffer,ByteBuffer> read(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(readBlockingQueue, buffer, Multiplexor.READ_OP);
        return this;
    }

    public Channel<ByteBuffer, ByteBuffer> write(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(writeBlockingQueue, buffer, Multiplexor.WRITE_OP);
        return this;
    }

    public Channel<ByteBuffer, ByteBuffer> flush() {
        assertState(State.OPEN);
        flush(writeBlockingQueue);
        return this;
    }

    private void enqueue(final BlockingQueue<ByteBuffer> blockingQueue, final ByteBuffer buffer, final int ops) {
        try {
            blockingQueue.put(buffer);
            if((interestOps & ops) == 0) {
                queueLock.lock();
                try {
                    if(!blockingQueue.isEmpty() && (interestOps & ops) == 0) {
                        multiplexor.register(multiplexorHandler, interestOps ^ ops);
                        interestOps |= ops;
                    }
                }
                finally {
                    queueLock.unlock();
                }
            }
        }
        catch(final InterruptedException e) {
            throw rethrow(e);
        }
    }

    private ByteBuffer dequeue(final BlockingQueue<ByteBuffer> blockingQueue, final int ops) {
        final ByteBuffer buffer = blockingQueue.poll();
        if(blockingQueue.isEmpty()) {
            queueLock.lock();
            try {
                if(blockingQueue.isEmpty() && (interestOps & ops) != 0) {
                    multiplexor.deregister(multiplexorHandler, interestOps & ops);
                    interestOps ^= ops;
                    emptyQueueCondition.signalAll();
                }
            }
            finally {
                queueLock.unlock();
            }
        }
        return buffer;
    }

    private void flush(final BlockingQueue<ByteBuffer> blockingQueue)
    {
        if(!blockingQueue.isEmpty()) {
            queueLock.lock();
            try {
                while(!blockingQueue.isEmpty()) {
                    emptyQueueCondition.await();
                }
            }
            catch(final InterruptedException e) {
                throw rethrow(e);
            }
            finally {
                queueLock.unlock();
            }
        }
    }

    @Override
    protected State onOpening() {
        try {
            if(socketChannel.isOpen()) {
                if(socketChannel.isConnected()) {
                    multiplexor.register(multiplexorHandler, Multiplexor.CLOSE_OP);
                    interestOps |= Multiplexor.CLOSE_OP;
                    return super.onOpening();
                }
                else if(!socketChannel.isConnectionPending()) {
                    multiplexor.register(multiplexorHandler, Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP);
                    interestOps |= Multiplexor.OPEN_OP| Multiplexor.CLOSE_OP;
                    socketChannel.connect(socketAddress);
                }
                return State.OPENING;
            }
            return State.CLOSING;
        }
        catch(final IOException e){
            throw new IoException(e);
        }
    }

    @Override
    protected State onOpen() {
        try {
            socketChannel.finishConnect();
            channelCallback.onOpen(this);
            multiplexor.deregister(multiplexorHandler, Multiplexor.OPEN_OP);
            interestOps ^= Multiplexor.OPEN_OP;
            return super.onOpen();
        }
        catch(final IOException e) {
            throw new IoException();
        }
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        channelCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    @Override
    protected State onClosing() {
        try {
            socketChannel.close();
            return super.onClosing();
        }
        catch(final IOException e) {
            throw new IoException(e);
        }
    }

    @Override
    protected State onClosed() {
        channelCallback.onClose(this);
        return super.onClosed();
    }

    private void doRead(final ByteBuffer buffer) {
        try {
            channelCallback.onRead(this, buffer);
        }
        catch(final Throwable throwable) {
            swallow(throwable);
        }
    }

    private void doWrite(final ByteBuffer buffer) {
        try {
            channelCallback.onWrite(this, buffer);
        }
        catch(final Throwable throwable) {
            swallow(throwable);
        }
    }

    private class MultiplexorHandler implements Multiplexor.Handler<ConnectorTcpChannel> {

        public ConnectorTcpChannel getChannel() {
            return ConnectorTcpChannel.this;
        }

        public void doOpen() {
            changeState(State.OPEN);
        }

        public int doRead(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = readBlockingQueue.peek(); buffer != null; buffer = readBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int size = NioChannel.Helper.read(socketChannel, buffer);
                        switch(size) {
                            case -1:
                                doClose();
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        ConnectorTcpChannel.this.doRead((ByteBuffer) dequeue(readBlockingQueue, Multiplexor.READ_OP).flip());
                        if(!buffer.hasRemaining() && (remaining > 0)) continue;
                    }
                    break;
                }
            }
            catch(final Throwable throwable) {
                changeState(State.ABORTING, throwable);
                swallow(throwable);
            }
            return length - remaining;
        }

        public int doWrite(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = writeBlockingQueue.peek(); buffer != null; buffer = writeBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int size = NioChannel.Helper.write(socketChannel, buffer);
                        switch(size) {
                            case -1:
                                doClose();
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        ConnectorTcpChannel.this.doWrite(dequeue(writeBlockingQueue, Multiplexor.WRITE_OP));
                        if(!buffer.hasRemaining() && (remaining > 0)) continue;
                    }
                    break;
                }
            }
            catch(final Throwable throwable) {
                changeState(State.ABORTING, throwable);
                swallow(throwable);
            }
            return length - remaining;
        }

        public void doClose() {
            changeState(State.CLOSING);
        }
    }
}