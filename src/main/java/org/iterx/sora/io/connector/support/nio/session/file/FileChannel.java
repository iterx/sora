package org.iterx.sora.io.connector.support.nio.session.file;

import org.iterx.sora.collection.queue.SingleProducerSingleConsumerBlockingQueue;
import org.iterx.sora.io.IoException;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class FileChannel extends AbstractChannel<ByteBuffer, ByteBuffer> implements NioChannel<java.nio.channels.FileChannel> {

    private final Multiplexor<? super FileChannel> multiplexor;
    private final ChannelCallback<? super FileChannel, ByteBuffer, ByteBuffer> channelCallback;
    private final java.nio.channels.FileChannel fileChannel;
    private final MultiplexorHandler multiplexorHandler;

    private final BlockingQueue<ByteBuffer> readBlockingQueue;
    private final BlockingQueue<ByteBuffer> writeBlockingQueue;

    private final Lock queueLock;
    private final Condition emptyQueueCondition;

    private volatile int interestOps;

    public FileChannel(final Multiplexor<? super FileChannel> multiplexor,
                       final ChannelCallback<? super FileChannel, ByteBuffer, ByteBuffer> channelCallback,
                       final java.nio.channels.FileChannel fileChannel) {
        this.readBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
        this.writeBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<ByteBuffer>(128);
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
        return (fileChannel.isOpen())? State.OPENED : State.CLOSING;
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

    private class MultiplexorHandler implements Multiplexor.Handler<FileChannel> {

        public FileChannel getChannel() {
            return FileChannel.this;
        }

        public void doOpen() {
            changeState(State.OPENED);
        }

        public int doRead(final int length) {
            int remaining = length;
            try {
                for(ByteBuffer buffer = readBlockingQueue.peek(); buffer != null; buffer = readBlockingQueue.peek()) {
                    OUTER: while(true) {
                        final int size = NioChannel.Helper.read(fileChannel, buffer);
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
                        FileChannel.this.doRead((ByteBuffer) dequeue(readBlockingQueue, Multiplexor.READ_OP).flip());
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
                        final int size = NioChannel.Helper.write(fileChannel, buffer);
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
                        FileChannel.this.doWrite(dequeue(writeBlockingQueue, Multiplexor.WRITE_OP));
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