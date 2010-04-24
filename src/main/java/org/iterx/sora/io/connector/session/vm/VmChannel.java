package org.iterx.sora.io.connector.session.vm;

import org.iterx.sora.collection.queue.SingleProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.io.connector.session.Channel;

import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class VmChannel extends AbstractChannel<ByteBuffer, ByteBuffer> {

    private final Multiplexor<? super VmChannel> multiplexor;
    private final ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback;
    private final MultiplexorHandler multiplexorHandler;

    private final BlockingQueue<ByteBuffer> readBlockingQueue;
    private final BlockingQueue<ByteBuffer> writeBlockingQueue;

    private final Lock queueLock;
    private final Condition emptyQueueCondition;

    private final VmChannel vmChannel;
    private volatile int interestOps;

    public VmChannel(final Multiplexor<? super VmChannel> multiplexor,
                     final ChannelCallback<? super VmChannel, ByteBuffer, ByteBuffer> channelCallback,
                     final VmChannel vmChannel) {
        this.readBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.writeBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.queueLock = new ReentrantLock();
        this.emptyQueueCondition = queueLock.newCondition();
        this.multiplexorHandler = new MultiplexorHandler();

        this.multiplexor = multiplexor;
        this.channelCallback = channelCallback;
        this.vmChannel = vmChannel;
    }

    public Channel<ByteBuffer,ByteBuffer> read(final ByteBuffer buffer) {
        assertState(State.OPENED);
        enqueue(readBlockingQueue, buffer, Multiplexor.READ_OP);
        return this;
    }

    public Channel<ByteBuffer, ByteBuffer> write(final ByteBuffer buffer) {
        assertState(State.OPENED);
        enqueue(writeBlockingQueue, buffer, Multiplexor.WRITE_OP);
        return this;
    }

    public Channel<ByteBuffer, ByteBuffer> flush() {
        assertState(State.OPENED);
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
    protected State onOpen() {
        channelCallback.onOpen(this);
        multiplexor.deregister(multiplexorHandler, Multiplexor.OPEN_OP);
        interestOps ^= Multiplexor.OPEN_OP;
        return super.onOpen();
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        channelCallback.onAbort(this, throwable);
        return super.onAbort(throwable);
    }

    @Override
    protected State onClose() {
        channelCallback.onClose(this);
        return super.onClose();
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

    private class MultiplexorHandler implements Multiplexor.Handler<VmChannel> {

        public VmChannel getChannel() {
            return VmChannel.this;
        }

        public void doOpen() {
            changeState(State.OPENED);
        }

        public int doRead(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = readBlockingQueue.peek(); buffer != null; buffer = readBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int position = buffer.position();
                        vmChannel.read(buffer);

                        final int size = buffer.position() - position;
                        switch(size) {
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        VmChannel.this.doRead((ByteBuffer) dequeue(readBlockingQueue, Multiplexor.READ_OP).flip());
                        if(!buffer.hasRemaining() && (remaining > 0)) continue;
                    }
                    break;
                }
            }
            catch(final Throwable throwable) {
                changeState(State.ABORTED, throwable);
                swallow(throwable);
            }
            return length - remaining;
        }

        public int doWrite(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = writeBlockingQueue.peek(); buffer != null; buffer = writeBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int position = buffer.position();
                        vmChannel.write(buffer);

                        final int size = buffer.position() - position;
                        switch(size) {
                            case 0:
                                break OUTER;
                            default:
                                remaining -= size;
                        }
                    }
                    if(buffer.position() != 0) {
                        VmChannel.this.doWrite(dequeue(writeBlockingQueue, Multiplexor.WRITE_OP));
                        if(!buffer.hasRemaining() && (remaining > 0)) continue;
                    }
                    break;
                }
            }
            catch(final Throwable throwable) {
                changeState(State.ABORTED, throwable);
                swallow(throwable);
            }
            return length - remaining;
        }

        public void doClose() {
            changeState(State.CLOSING);
        }
    }
}