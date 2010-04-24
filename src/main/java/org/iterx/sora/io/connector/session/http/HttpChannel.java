package org.iterx.sora.io.connector.session.http;

import org.iterx.sora.collection.queue.CircularBlockingQueue;
import org.iterx.sora.collection.queue.SingleProducerSingleConsumerCircularBlockingQueue;
import org.iterx.sora.io.connector.session.AbstractChannel;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.http.message.HttpMessage;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

public final class HttpChannel<R extends HttpMessage, W extends HttpMessage> extends AbstractChannel<R, W> {

    private static final EOFException EOF_EXCEPTION = new EOFException();
    private static final int BUFFER_SIZE = 1024;
    private static final int BUFFER_COUNT = 8;

    private final ChannelCallback<? super HttpChannel<R, W>, R, W> channelCallback;
    private final DelegateChannel delegateChannel;

    public HttpChannel(final Session<?, ByteBuffer, ByteBuffer> session,
                       final ChannelCallback<? super HttpChannel<R, W>, R, W> channelCallback) {
        this.channelCallback = channelCallback;
        this.delegateChannel = new DelegateChannel(session, 4);
    }

    public Channel<R, W> read(final R httpMessage) {
        assertState(State.OPEN);
        delegateChannel.read(httpMessage);
        return this;
    }

    public Channel<R, W> write(final W httpMessage) {
        assertState(State.OPEN);
        delegateChannel.write(httpMessage);
        return this;
    }

    public Channel<R, W> flush() {
        assertState(State.OPEN);
        delegateChannel.flush();
        return this;
    }

    @Override
    protected State onOpening() {
        delegateChannel.open();
        return null;
    }

    @Override
    protected State onOpen() {
        channelCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onClosing() {
        delegateChannel.close();
        return State.CLOSING;
    }

    @Override
    protected State onClosed() {
        channelCallback.onClose(this);
        return super.onClosed();
    }

    private void doRead(final R httpMessage) {
        try {
            channelCallback.onRead(this, httpMessage);
        }
        catch(final Throwable throwable) {
            swallow(throwable);
        }
    }

    private void doWrite(final W httpMessage) {
        try {
            channelCallback.onWrite(this, httpMessage);
        }
        catch(final Throwable throwable) {
            swallow(throwable);
        }
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        return super.onAbort(throwable);
    }

    //TODO: Note this is not thread safe
    private class DelegateChannel implements Channel<R, W>,
                                             ChannelCallback<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer> {

        private final Channel<ByteBuffer, ByteBuffer> channel;
        private final Decoder decoder;
        private final Encoder encoder;

        private DelegateChannel(final Session<?, ByteBuffer, ByteBuffer> session, final int capacity) {
            this.channel = session.newChannel(this);
            this.decoder = new Decoder();
            this.encoder = new Encoder();
        }

        public Channel<R, W> open() {
            channel.open();
            return this;
        }

        public Channel<R, W> read(final R httpMessage) {
            decoder.enqueue(httpMessage);
            return this;
        }

        public Channel<R, W> write(final W httpMessage) {
            encoder.enqueue(httpMessage);
            return this;
        }

        public Channel<R, W> flush() {
            channel.flush();
            return this;
        }

        public Channel<R, W> close() {
            channel.close();
            return this;
        }

        public void onOpen(final Channel<ByteBuffer, ByteBuffer> channel) {
            changeState(State.OPEN);
        }

        public void onWrite(final Channel<ByteBuffer, ByteBuffer> channel, final ByteBuffer buffer) {
            encoder.dequeue(buffer);
        }

        public void onRead(final Channel<ByteBuffer, ByteBuffer> channel, final ByteBuffer buffer) {
            decoder.dequeue(buffer);
        }

        public void onAbort(final Channel<ByteBuffer, ByteBuffer> channel, final Throwable throwable) {
            changeState(State.ABORTING, throwable);
        }

        public void onClose(final Channel<ByteBuffer, ByteBuffer> channel) {
            changeState(State.CLOSED);
        }

        private void doRead(final ByteBuffer buffer) {
            channel.read((ByteBuffer) buffer.clear());
        }

        private void doWrite(final ByteBuffer buffer) {
            channel.write((ByteBuffer) buffer.flip());
        }

        private final class Decoder {

            private final CircularBlockingQueue<ByteBuffer> byteBufferCircularBlockingQueue;
            private final CircularBlockingQueue<R> httpMessageCircularBlockingQueue;
            private final DecoderDataInput decoderDataInput;
            private final Lock decoderLock;

            private Decoder() {
                this.byteBufferCircularBlockingQueue = allocateByteBuffers(new SingleProducerSingleConsumerCircularBlockingQueue<ByteBuffer>(BUFFER_COUNT, false));
                this.httpMessageCircularBlockingQueue = new SingleProducerSingleConsumerCircularBlockingQueue<R>(128, false);
                this.decoderDataInput = new DecoderDataInput();
                this.decoderLock = new ReentrantLock();
            }

            private void enqueue(final R httpMessage) {
                try {
                    httpMessageCircularBlockingQueue.put(httpMessage);
                    doDecode();
                }
                catch(final InterruptedException e) {
                    throw rethrow(e);
                }
            }

            private void dequeue(final ByteBuffer buffer) {
                try {
                    byteBufferCircularBlockingQueue.put(buffer);
                    doDecode();
                }
                catch(final InterruptedException e) {
                    throw rethrow(e);
                }
            }

            private void doDecode() {
                if(decoderLock.tryLock()) {
                    try {
                        int count = 0;
                        if(!httpMessageCircularBlockingQueue.isEmpty()) {
                            for(R httpMessage = httpMessageCircularBlockingQueue.peek(); httpMessage != null; httpMessage = httpMessageCircularBlockingQueue.peek()) {
                                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                                if(byteBuffer != null) {
                                    try {
                                        byteBuffer.mark();
                                        httpMessage.decode(decoderDataInput);
                                        //TODO: Change back to normal blocking queue & test perf
                                        httpMessageCircularBlockingQueue.poll();
                                        count++;
                                        continue;
                                    }
                                    catch(final IOException e) {
                                        rewindByteBuffers(byteBuffer);
                                    }
                                }
                                break;
                            }
                            flushHttpMessages(count);
                            flushByteBuffers();
                        }
                    }
                    finally {
                        decoderLock.unlock();
                    }
                }
            }

            private void flushHttpMessages(final int count) {
                for(int i = count; i-- != 0;) HttpChannel.this.doRead(httpMessageCircularBlockingQueue.remove());
            }

            private ByteBuffer getByteBuffer(final int sizeOf) throws IOException {
                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                if(byteBuffer != null) {
                    final int remaining = byteBuffer.remaining();
                    if(remaining == 0) {
                        byteBufferCircularBlockingQueue.poll();
                        return getByteBuffer(sizeOf);
                    }
                    else if(byteBuffer.remaining() < sizeOf) {
                        //TODO: Need to fixup buffers -> data wraps boundary
                        throw new UnsupportedOperationException();
                    }
                    return byteBuffer;
                }
                throw EOF_EXCEPTION;
            }

            private boolean broken = false;

            private void rewindByteBuffers(final ByteBuffer toByteBuffer) {
                for(long index = byteBufferCircularBlockingQueue.index(); ;) {
                    final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.get(index);
                    if(byteBuffer == toByteBuffer) {
                        byteBuffer.reset();
                        break;
                    }
                    else {
                        byteBuffer.rewind();
                        byteBufferCircularBlockingQueue.rewind(--index);
                    }
                }
            }

            private void flushByteBuffers() {
                for(int i = byteBufferCircularBlockingQueue.capacity() - byteBufferCircularBlockingQueue.remainingCapacity() - byteBufferCircularBlockingQueue.size(); i > 0; i--)
                    doRead((ByteBuffer) byteBufferCircularBlockingQueue.remove().clear());
            }

            private <T extends BlockingQueue<ByteBuffer>> T allocateByteBuffers(final T blockingQueue) {
                while(blockingQueue.remainingCapacity() != 0) {
                    blockingQueue.add(ByteBuffer.allocate(BUFFER_SIZE));
                    blockingQueue.poll();
                }
                return blockingQueue;
            }

            private final class DecoderDataInput implements DataInput {

                public void readFully(final byte[] bytes) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void readFully(final byte[] bytes, final int offset, final int length) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public int skipBytes(final int length) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public boolean readBoolean() throws IOException {
                    return (getByteBuffer(1).get() == 1);
                }

                public byte readByte() throws IOException {
                    return getByteBuffer(1).get();
                }

                public int readUnsignedByte() throws IOException {
                    return getByteBuffer(1).get();
                }

                public short readShort() throws IOException {
                    return getByteBuffer(2).getShort();
                }

                public int readUnsignedShort() throws IOException {
                    return getByteBuffer(2).getShort();
                }

                public char readChar() throws IOException {
                    return getByteBuffer(2).getChar();
                }

                public int readInt() throws IOException {
                    return getByteBuffer(4).getInt();
                }

                public long readLong() throws IOException {
                    return getByteBuffer(8).getLong();
                }

                public float readFloat() throws IOException {
                    return getByteBuffer(4).getFloat();
                }

                public double readDouble() throws IOException {
                    return getByteBuffer(8).getDouble();
                }

                public String readLine() throws IOException {
                    throw new UnsupportedOperationException();
                }

                public String readUTF() throws IOException {
                    throw new UnsupportedOperationException();
                }
            }
        }

        private final class Encoder  {

            private final CircularBlockingQueue<ByteBuffer> byteBufferCircularBlockingQueue;
            private final CircularBlockingQueue<W> httpMessageCircularBlockingQueue;
            private final EncoderDataOutput encoderDataOutput;
            private final Lock encoderLock;

            private final int[] byteBufferHttpMessageCounts;
            private volatile int byteBufferHttpMessageCountIndex;

            private Encoder() {
                this.byteBufferCircularBlockingQueue = allocateByteBuffers(new SingleProducerSingleConsumerCircularBlockingQueue<ByteBuffer>(BUFFER_COUNT, false));
                this.byteBufferHttpMessageCounts = new int[BUFFER_COUNT];
                this.encoderDataOutput = new EncoderDataOutput();
                this.httpMessageCircularBlockingQueue = new SingleProducerSingleConsumerCircularBlockingQueue<W>(128, false);
                this.encoderLock = new ReentrantLock();
            }

            private void enqueue(final W httpMessage) {
                try {
                    httpMessageCircularBlockingQueue.put(httpMessage);
                    doEncode();
                }
                catch(final InterruptedException e) {
                    throw rethrow(e);
                }
            }

            private void dequeue(final ByteBuffer buffer) {
                try {
                    byteBufferCircularBlockingQueue.put((ByteBuffer) buffer.clear());
                    flushHttpMessages(drainByteBufferHttpMessageCount());
                    doEncode();
                }
                catch(final InterruptedException e) {
                    throw rethrow(e);
                }
            }

            private void doEncode() {
                if(encoderLock.tryLock()) {
                    try {
                        if(!httpMessageCircularBlockingQueue.isEmpty()) {
                            for(W httpMessage = httpMessageCircularBlockingQueue.peek(); httpMessage != null; httpMessage = httpMessageCircularBlockingQueue.peek()) {
                                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                                if(byteBuffer != null) {
                                    try {
                                        byteBuffer.mark();
                                        httpMessage.encode(encoderDataOutput);
                                        //TODO: should signal if more data outstanding -> i.e multipart
                                        //TODO: only poll once message is completely finished!
                                        incrementByteBufferHttpMessageCount();
                                        httpMessageCircularBlockingQueue.poll();
                                        continue;
                                    }
                                    catch(final IOException e) {
                                        rewindByteBuffers(byteBuffer);
                                    }
                                }
                                break;
                            }
                            flushByteBuffers();
                        }
                    }
                    finally {
                        encoderLock.unlock();
                    }
                }
            }

            private void flushHttpMessages(final int count) {
                for(int i = count; i-- != 0;)HttpChannel.this.doWrite(httpMessageCircularBlockingQueue.remove());
            }

            private ByteBuffer getByteBuffer(final int sizeOf) throws IOException {
                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                if(byteBuffer != null) {
                    if(byteBuffer.remaining() < sizeOf) {
                        byteBufferCircularBlockingQueue.poll();
                        nextByteBufferHttpMessageCount();
                        return getByteBuffer(sizeOf);
                    }
                    return byteBuffer;
                }
                throw EOF_EXCEPTION;
            }

            private void flushByteBuffers() {
                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                if(byteBuffer != null && byteBuffer.position() != 0) {
                    byteBufferCircularBlockingQueue.poll();
                    nextByteBufferHttpMessageCount();
                }
                for(int i = byteBufferCircularBlockingQueue.capacity() - byteBufferCircularBlockingQueue.remainingCapacity() - byteBufferCircularBlockingQueue.size(); i > 0; i--)
                    doWrite(byteBufferCircularBlockingQueue.remove());
            }

            private <T extends BlockingQueue<ByteBuffer>> T allocateByteBuffers(final T blockingQueue) {
                while(blockingQueue.remainingCapacity() != 0) blockingQueue.add(ByteBuffer.allocate(BUFFER_SIZE));
                return blockingQueue;
            }

            private void rewindByteBuffers(final ByteBuffer toByteBuffer) {
                for(long index = byteBufferCircularBlockingQueue.index(); ;) {
                    final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.get(index);
                    if(byteBuffer == toByteBuffer) {
                        byteBuffer.reset();
                        break;
                    }
                    else {
                        byteBuffer.rewind();
                        byteBufferCircularBlockingQueue.rewind(--index);
                    }
                }
            }

            private void nextByteBufferHttpMessageCount() {
                byteBufferHttpMessageCountIndex = ++byteBufferHttpMessageCountIndex % byteBufferHttpMessageCounts.length;
            }

            private void incrementByteBufferHttpMessageCount() {
                byteBufferHttpMessageCounts[byteBufferHttpMessageCountIndex]++;
            }

            private int drainByteBufferHttpMessageCount() {
                final int index = (byteBufferHttpMessageCountIndex + byteBufferHttpMessageCounts.length - 1) % byteBufferHttpMessageCounts.length;
                final int count = byteBufferHttpMessageCounts[index];
                byteBufferHttpMessageCounts[index] = 0;
                return count;
            }

            private final class EncoderDataOutput implements DataOutput {

                public void write(final int b) throws IOException {
                    getByteBuffer(4).put((byte) b);
                }

                public void write(final byte[] bytes) throws IOException {
                    write(bytes, 0, bytes.length);
                }

                public void write(final byte[] bytes, final int offset, final int length) throws IOException {
                    //TODO: should iterate and use multiple byte buffers!
                    getByteBuffer(length).put(bytes, offset, length);
                }

                public void writeBoolean(final boolean value) throws IOException {
                    getByteBuffer(1).put((value)? (byte) 1 : (byte) 0);
                }

                public void writeByte(final int value) throws IOException {
                    getByteBuffer(1).put((byte) value);
                }

                public void writeShort(final int value) throws IOException {
                    getByteBuffer(2).putShort((short) value);
                }

                public void writeChar(final int value) throws IOException {
                    getByteBuffer(2).putChar((char) value);
                }

                public void writeInt(final int value) throws IOException {
                    getByteBuffer(4).putInt(value);
                }

                public void writeLong(final long value) throws IOException {
                    getByteBuffer(8).putLong(value);
                }

                public void writeFloat(final float value) throws IOException {
                    getByteBuffer(4).putFloat(value);
                }

                public void writeDouble(final double value) throws IOException {
                    getByteBuffer(8).putDouble(value);
                }

                public void writeBytes(final String string) throws IOException {
                    for(int i = 0, length = string.length(); i != length; i++) write(string.charAt(i));
                }

                public void writeChars(final String string) throws IOException {
                    for(int i = 0, length = string.length(); i != length; i++) writeChar(string.charAt(i));
                }

                public void writeUTF(final String string) throws IOException {
                    throw new UnsupportedOperationException();
                }
            }
        }
    }
}
