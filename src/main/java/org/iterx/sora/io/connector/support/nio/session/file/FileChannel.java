package org.iterx.sora.io.connector.support.nio.session.file;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.collection.queue.MultiProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class FileChannel extends AbstractChannel<ByteBuffer> implements NioChannel<java.nio.channels.FileChannel> {

    private final Multiplexor<? super FileChannel> multiplexor;
    private final Callback<? super FileChannel, ByteBuffer> channelCallback;
    private final java.nio.channels.FileChannel fileChannel;
    private final MultiplexorHandler multiplexorHandler;

    private final MultiProducerSingleConsumerBlockingQueue<ByteBuffer> readBlockingQueue;
    private final MultiProducerSingleConsumerBlockingQueue<ByteBuffer> writeBlockingQueue;

    private final Lock queueLock;
    private final Condition emptyQueueCondition;

    private volatile int interestOps;

    public FileChannel(final Multiplexor<? super FileChannel> multiplexor,
                       final Callback<? super FileChannel, ByteBuffer> channelCallback,
                       final java.nio.channels.FileChannel fileChannel) {
        this.readBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.writeBlockingQueue = new MultiProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.queueLock = new ReentrantLock();
        this.emptyQueueCondition = queueLock.newCondition();
        this.multiplexorHandler = new MultiplexorHandler();

        this.multiplexor = multiplexor;
        this.channelCallback = channelCallback;
        this.fileChannel = fileChannel;
    }

    public java.nio.channels.FileChannel getChannel() {
        return fileChannel;
    }

    public void read(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(readBlockingQueue, buffer, Multiplexor.READ_OP);
    }

    public void write(final ByteBuffer buffer) {
        assertState(State.OPEN);
        enqueue(writeBlockingQueue, buffer, Multiplexor.WRITE_OP);
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
        queueLock.lock();
        try {
            while(!blockingQueue.isEmpty()) {
                emptyQueueCondition.await();
            }
            fileChannel.force(true);
        }
        catch(final InterruptedException e) {
            throw rethrow(e);
        }
        catch(final IOException e) {
            throw new IoException(e);
        }
        finally {
            queueLock.unlock();
        }
    }

    @Override
    protected State onOpening() {
        return (fileChannel.isOpen())? State.OPEN : State.CLOSING;
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
            fileChannel.close();
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

    private class MultiplexorHandler implements Multiplexor.Handler<FileChannel> {

        public FileChannel getChannel() {
            return FileChannel.this;
        }

        public void doOpen() {
            changeState(State.OPEN);
        }

        public int doRead(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = readBlockingQueue.peek(); buffer != null; buffer = readBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int size = fileChannel.read(buffer);
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
                        FileChannel.this.doRead(dequeue(readBlockingQueue, Multiplexor.READ_OP));
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
                        final int size = fileChannel.write(buffer);
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
                        FileChannel.this.doWrite(dequeue(writeBlockingQueue, Multiplexor.WRITE_OP));
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