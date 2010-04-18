package org.iterx.sora.io.connector.session.http;

import org.iterx.sora.collection.queue.CircularBlockingQueue;
import org.iterx.sora.collection.queue.SingleProducerSingleConsumerBlockingQueue;
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

import static org.iterx.sora.util.Exception.rethrow;
import static org.iterx.sora.util.Exception.swallow;

//TODO: represent reusable connection to server -> can pipe HTTP Requests over it... -> alter path but
//TODO: not connection info

public final class HttpChannel<R extends HttpMessage, W extends HttpMessage> extends AbstractChannel<R, W> {

    private static final int CAPACITY = 4;
    private static final int BUFFER_SIZE = 4096;

    private final ChannelCallback<? super HttpChannel<R, W>, R, W> channelCallback;
    private final DelegateChannel delegateChannel;

    public HttpChannel(final Session<?, ByteBuffer, ByteBuffer> session,
                       final ChannelCallback<? super HttpChannel<R, W>, R, W> channelCallback) {
        this.channelCallback = channelCallback;
        this.delegateChannel = new DelegateChannel(session, 4); //TODO -> this should contain types!
    }

    public void read(final R httpMessage) {
        assertState(State.OPEN);
        delegateChannel.read(httpMessage);
    }

    public void write(final W httpMessage) {
        assertState(State.OPEN);
        delegateChannel.write(httpMessage);
    }

    public void flush() {
        assertState(State.OPEN);
        delegateChannel.flush();
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
        channelCallback.onRead(this, httpMessage);
    }

    private void doWrite(final W httpMessage) {
        channelCallback.onWrite(this, httpMessage);
    }

    @Override
    protected State onAbort(final Throwable throwable) {
        return super.onAbort(throwable);
    }

    //TODO: note this is not thread safe
    //TODO: Make this request response type aware!!!!
    private class DelegateChannel implements Channel<R, W>,
                                             ChannelCallback<Channel<ByteBuffer, ByteBuffer>, ByteBuffer, ByteBuffer> {

        private final Channel<ByteBuffer, ByteBuffer> channel;
        private final Encoder encoder;
        private final Decoder decoder;

        private final int capacity;

        private DelegateChannel(final Session<?, ByteBuffer, ByteBuffer> session, final int capacity) {
            this.encoder = new Encoder();
            this.decoder = new Decoder();
            this.channel = session.newChannel(this);
            this.capacity = capacity;
        }

        public void open() {
            channel.open();
        }

        public void read(final R httpMessage) {
            decoder.enqueue(httpMessage);
        }

        public void write(final W httpMessage) {
            encoder.enqueue(httpMessage);
        }

        public void flush() {
            channel.flush();
        }

        public void close() {
            channel.close();
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
            private final BlockingQueue<HttpMessage> httpMessageBlockingQueue;
            private final DecoderDataInput decoderDataInput;

            private Decoder() {
                this.byteBufferCircularBlockingQueue = new SingleProducerSingleConsumerCircularBlockingQueue<ByteBuffer>(16, false);
                this.httpMessageBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<HttpMessage>(16);
                this.decoderDataInput = new DecoderDataInput();
            }

            private void enqueue(final HttpMessage httpMessage) {
                httpMessageBlockingQueue.add(httpMessage);
                try {
                    httpMessage.decode(decoderDataInput);
                    flush(httpMessage);
                }
                catch(final Exception e) {
                    //e.printStackTrace();
                    //swallow(e);
                }
            }

            private void dequeue(final ByteBuffer buffer) {
                try {
                    byteBufferCircularBlockingQueue.put(buffer);
                    final HttpMessage httpMessage  = httpMessageBlockingQueue.peek();
                     if(httpMessage != null) {
                         httpMessage.decode(decoderDataInput);
                         flush(httpMessageBlockingQueue.poll());
                     }
                }
                catch(final Exception e) {
                    //e.printStackTrace();
                    swallow(e);
                }
            }

            private void flush(final HttpMessage httpMessage) {
                HttpChannel.this.doRead((R) httpMessage);
            }

            private ByteBuffer getByteBuffer() throws IOException {
                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                if(byteBuffer != null) {
                    if(!byteBuffer.hasRemaining()) {
                        byteBufferCircularBlockingQueue.poll();
                        doRead(byteBufferCircularBlockingQueue.remove());
                        return getByteBuffer();
                    }
                    return byteBuffer;
                }
                return allocateByteBuffer(byteBufferCircularBlockingQueue);
            }

            private ByteBuffer allocateByteBuffer(final BlockingQueue<ByteBuffer> byteBufferBlockQueue) throws IOException {
                final ByteBuffer byteBuffer = ByteBuffer.allocate(4096);
                doRead(byteBuffer);
                throw new EOFException();
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
                    return (getByteBuffer().get() == 1);
                }

                public byte readByte() throws IOException {
                    return getByteBuffer().get();
                }

                public int readUnsignedByte() throws IOException {
                    return getByteBuffer().get();
                }

                public short readShort() throws IOException {
                    return getByteBuffer().getShort();
                }

                public int readUnsignedShort() throws IOException {
                    return getByteBuffer().getShort();
                }

                public char readChar() throws IOException {
                    return getByteBuffer().getChar();
                }

                public int readInt() throws IOException {
                    return getByteBuffer().getInt();
                }

                public long readLong() throws IOException {
                    return getByteBuffer().getLong();
                }

                public float readFloat() throws IOException {
                    return getByteBuffer().getFloat();
                }

                public double readDouble() throws IOException {
                    return getByteBuffer().getDouble();
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
            private final BlockingQueue<HttpMessage> httpMessageBlockingQueue;
            private final EncoderDataOutput encoderDataOutput;

            private Encoder() {
                this.byteBufferCircularBlockingQueue = new SingleProducerSingleConsumerCircularBlockingQueue<ByteBuffer>(16, false);
                this.httpMessageBlockingQueue = new SingleProducerSingleConsumerBlockingQueue<HttpMessage>(16);
                this.encoderDataOutput = new EncoderDataOutput();
            }

            //TODO: every time we use a byteBuffer -> we push the httpMessage onto the pending queue
            private void enqueue(final HttpMessage httpMessage) {
                try {
                    httpMessage.encode(encoderDataOutput);
                    flush(httpMessage);
                }
                catch(final IOException e) {
                    throw rethrow(e);
                }
            }

            private void dequeue(final ByteBuffer byteBuffer) {
                try {
                    byteBufferCircularBlockingQueue.put((ByteBuffer) byteBuffer.clear());
                    HttpChannel.this.doWrite((W) httpMessageBlockingQueue.poll());
                }
                catch(final Exception e) {
                    swallow(e);
                }
            }

            private void flush(final HttpMessage httpMessage) {
                //TODO: If spanning multiple bytebuffers -> this might be null or placeholder msg
                httpMessageBlockingQueue.add(httpMessage);
                byteBufferCircularBlockingQueue.poll();
                doWrite(byteBufferCircularBlockingQueue.remove());
            }

            //TODO: delegate to bytebuffer -> assign new buffer upto capacity if run out... -> otherwise
            //TODO: rewind and throw exception
            //TODO: If reached overall capacity -> throw fatal message too large exception...
            private ByteBuffer getByteBuffer(final int sizeOf) throws IOException {
                final ByteBuffer byteBuffer = byteBufferCircularBlockingQueue.peek();
                if(byteBuffer != null) {
                    if(byteBuffer.remaining() >= sizeOf) return byteBuffer;
                    flush(null);
                }
                return allocateByteBuffer(byteBufferCircularBlockingQueue);
            }

            private ByteBuffer allocateByteBuffer(final BlockingQueue<ByteBuffer> blockingQueue) throws IOException {
                try {
                    final ByteBuffer byteBuffer = ByteBuffer.allocate(4096);//TODO: expose as configuration
                    blockingQueue.put(byteBuffer); //TODO: need to internally count inflight buffer capactity
                    return byteBuffer;
                }
                catch(final InterruptedException e) {
                    throw new EOFException();
                }
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
