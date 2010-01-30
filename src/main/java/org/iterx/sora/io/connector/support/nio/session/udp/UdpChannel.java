package org.iterx.sora.io.connector.support.nio.session.udp;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.support.nio.session.AbstractChannel;
import org.iterx.sora.io.connector.support.nio.strategy.MultiplexorStrategy;
import org.iterx.sora.util.collection.queue.MultiProducerSingleConsumerBlockingQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class UdpChannel extends AbstractChannel {

    private final MultiplexorStrategy<? super DatagramChannel> multiplexorStrategy;
    private final Callback<? super UdpChannel, ByteBuffer> channelCallback;
    private final DatagramChannel datagramChannel;
    private final MultiplexorHandler multiplexorHandler;

    private final MultiProducerSingleConsumerBlockingQueue<ByteBuffer> readBlockingQueue;
    private final MultiProducerSingleConsumerBlockingQueue<ByteBuffer> writeBlockingQueue;

    private final Lock queueLock;
    private final Condition emptyQueueCondition;

    private volatile int interestOps;

    public UdpChannel(final MultiplexorStrategy<? super DatagramChannel> multiplexorStrategy,
                      final Callback<? super UdpChannel, ByteBuffer> channelCallback,
                      final DatagramChannel datagramChannel) {
        this.readBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.writeBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.queueLock = new ReentrantLock();
        this.emptyQueueCondition = queueLock.newCondition();
        this.multiplexorHandler = new MultiplexorHandler();

        this.multiplexorStrategy = multiplexorStrategy;
        this.channelCallback = channelCallback;
        this.datagramChannel = datagramChannel;
    }

    public void read(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(readBlockingQueue, buffer, MultiplexorStrategy.READ_OP);
    }

    public void write(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(writeBlockingQueue, buffer, MultiplexorStrategy.WRITE_OP);
    }

    public void flush() {
        assertState(State.OPEN);
        flush(writeBlockingQueue);
    }


    private void enqueue(final BlockingQueue<ByteBuffer> blockingQueue, final ByteBuffer buffer, final int ops) {
        try {
            blockingQueue.put(buffer);
            if((interestOps & ops) == 0) {
                queueLock.lock();
                try {
                    if(!blockingQueue.isEmpty() && (interestOps & ops) == 0) {
                        multiplexorStrategy.register(multiplexorHandler, interestOps ^ ops);
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
                    multiplexorStrategy.deregister(multiplexorHandler, interestOps & ops);
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
        if(datagramChannel.isOpen()) {
            multiplexorStrategy.register(multiplexorHandler, MultiplexorStrategy.CLOSE_OP);
            interestOps |= MultiplexorStrategy.CLOSE_OP;
            return State.OPEN;
        }
        return State.CLOSING;
    }

    @Override
    protected State onOpen() {
        channelCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        channelCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    @Override
    protected State onClosing() {
        try {
            datagramChannel.close();
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
        channelCallback.onRead(this, buffer);
    }

    private void doWrite(final ByteBuffer buffer) {
        channelCallback.onWrite(this, buffer);
    }

    private class MultiplexorHandler implements MultiplexorStrategy.MultiplexorHandler<DatagramChannel> {

        public DatagramChannel getChannel() {
            return datagramChannel;
        }

        public void doOpen() {
            changeState(State.OPEN);
        }

        public int doRead(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = readBlockingQueue.peek(); buffer != null; buffer = readBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int size = datagramChannel.read(buffer);
                        switch(size) {
                            case -1:
                                throw new ClosedChannelException();
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        UdpChannel.this.doRead(dequeue(readBlockingQueue, MultiplexorStrategy.READ_OP));
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
                        final int size = datagramChannel.write(buffer);
                        switch(size) {
                            case -1:
                                throw new ClosedChannelException();
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        UdpChannel.this.doWrite(dequeue(writeBlockingQueue, MultiplexorStrategy.WRITE_OP));
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
