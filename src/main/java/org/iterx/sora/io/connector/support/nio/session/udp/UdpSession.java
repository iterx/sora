package org.iterx.sora.io.connector.support.nio.session.udp;

import org.iterx.sora.io.IoException;
import org.iterx.sora.io.Uri;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.AbstractSession;
import org.iterx.sora.collection.Map;
import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.MembershipKey;
import java.nio.channels.spi.SelectorProvider;
import java.util.Iterator;
import java.util.Set;

public final class UdpSession extends AbstractSession<UdpChannel, ByteBuffer, ByteBuffer>  {
    
    private final Multiplexor<? super NioChannel> multiplexor;
    private final SessionCallback<? super UdpSession> sessionCallback;
    private final UdpChannelProvider udpChannelProvider;

    public UdpSession(final Multiplexor<? super NioChannel> multiplexor,
                      final SessionCallback<? super UdpSession> sessionCallback,
                      final AcceptorEndpoint acceptorEndpoint) {
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
        this.udpChannelProvider = new AcceptorUdpChannelProvider(acceptorEndpoint);
    }

    public UdpSession(final Multiplexor<? super NioChannel> multiplexor,
                      final SessionCallback<? super UdpSession> sessionCallback,
                      final ConnectorEndpoint connectorEndpoint) {
        this.udpChannelProvider = new ConnectorUdpChannelProvider(connectorEndpoint);
        this.multiplexor = multiplexor;
        this.sessionCallback = sessionCallback;
    }

    public UdpChannel newChannel(final Channel.ChannelCallback<? super UdpChannel, ByteBuffer, ByteBuffer> channelCallback) {
        return udpChannelProvider.newChannel(channelCallback);
    }

    @Override
    protected State onOpening() {
        udpChannelProvider.open();
        return super.onOpening();
    }

    @Override
    protected State onOpen() {
        sessionCallback.onOpen(this);
        return super.onOpen();
    }

    @Override
    protected State onClosing() {
        udpChannelProvider.close();
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

    private static abstract class UdpChannelProvider {

        public void open() {
        }

        abstract UdpChannel newChannel(Channel.ChannelCallback<? super UdpChannel, ByteBuffer, ByteBuffer> channelCallback);

        public void close() {
        }

        protected SocketAddress toSocketAddress(final Uri uri) {
            return new InetSocketAddress(uri.getHost(), uri.getPort());
        }

        protected DatagramChannel newDatagramChannel() {
            try {
                final DatagramChannel datagramChannel = DatagramChannel.open();
                datagramChannel.configureBlocking(false);
                datagramChannel.setOption(StandardSocketOption.SO_REUSEADDR, true);
                return datagramChannel;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }
    }

    private final class ConnectorUdpChannelProvider extends UdpChannelProvider {

        private final SocketAddress socketAddress;

        private ConnectorUdpChannelProvider(final ConnectorEndpoint connectorEndpoint){
            this.socketAddress = toSocketAddress(connectorEndpoint.getUri());
        }

        public UdpChannel newChannel(final Channel.ChannelCallback<? super UdpChannel, ByteBuffer, ByteBuffer> channelCallback) {
            try {
                final DatagramChannel datagramChannel = newDatagramChannel().connect(socketAddress);

                return new UdpChannel(multiplexor, channelCallback, datagramChannel);
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }
    }

    private final class AcceptorUdpChannelProvider extends UdpChannelProvider {

        private final Map<SocketAddress, UdpChannel> udpChannelBySocketAddress;
        private final AcceptorUdpChannel acceptorUdpChannel;
        private final ProxyMultiplexor proxyMultiplexor;

        private volatile Iterator<SocketAddress> pollSocketAddressIterator;

        private AcceptorUdpChannelProvider(final AcceptorEndpoint acceptorEndpoint) {
            this.udpChannelBySocketAddress = new HashMap<SocketAddress, UdpChannel>();
            this.proxyMultiplexor = new ProxyMultiplexor();
            this.acceptorUdpChannel = new AcceptorUdpChannel(newDatagramChannel(),
                                                             toSocketAddress(acceptorEndpoint.getUri()));
        }

        @Override
        public void open() {
            acceptorUdpChannel.open();
        }

        public UdpChannel newChannel(final Channel.ChannelCallback<? super UdpChannel, ByteBuffer, ByteBuffer> channelCallback) {
            try {
                final DatagramChannel datagramChannel = acceptorUdpChannel.accept();
                final UdpChannel udpChannel = new UdpChannel(proxyMultiplexor, channelCallback, datagramChannel);
                udpChannelBySocketAddress.put(datagramChannel.getRemoteAddress(), udpChannel);
                pollSocketAddressIterator = null;
                return udpChannel;
            }
            catch(final IOException e) {
                throw new IoException(e);
            }
        }

        @Override
        public void close() {
            acceptorUdpChannel.close();
        }

        private SocketAddress poll() {
            if(pollSocketAddressIterator == null) pollSocketAddressIterator = udpChannelBySocketAddress.keySet().iterator();
            if(!pollSocketAddressIterator.hasNext()) {
                pollSocketAddressIterator = null;
                return null;
            }
            return pollSocketAddressIterator.next();
        }

        private boolean doAccept(final SocketAddress socketAddress) {
            if(!udpChannelBySocketAddress.containsKey(socketAddress)) {
                UdpSession.this.doAccept();
                return udpChannelBySocketAddress.containsKey(socketAddress);
            }
            return true;
        }

        private void doClose(final SocketAddress socketAddress) {
            udpChannelBySocketAddress.remove(socketAddress);
            pollSocketAddressIterator = null;
        }

        private class AcceptorUdpChannel implements NioChannel<DatagramChannel> {

            private final DatagramChannel datagramChannel;
            private final SocketAddress localSocketAddress;
            private final MultiplexorHandler multiplexorHandler;

            private AcceptorUdpChannel(final DatagramChannel datagramChannel,
                                       final SocketAddress socketAddress) {

                this.multiplexorHandler = new MultiplexorHandler();
                this.datagramChannel = datagramChannel;
                this.localSocketAddress = socketAddress;
            }

            public DatagramChannel getChannel() {
                return datagramChannel;
            }

            public Channel<ByteBuffer, ByteBuffer> open() {
                try {
                    datagramChannel.bind(localSocketAddress);
                    multiplexor.register(multiplexorHandler, Multiplexor.READ_OP| Multiplexor.WRITE_OP| Multiplexor.CLOSE_OP);
                    return this;
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }

            public DatagramChannel accept() {
                return  new ProxyDatagramChannel(multiplexorHandler.accept());
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

            public Channel<ByteBuffer, ByteBuffer> close() {
                try {
                    multiplexor.deregister(multiplexorHandler, Multiplexor.READ_OP| Multiplexor.WRITE_OP| Multiplexor.CLOSE_OP);
                    datagramChannel.close();
                    return this;
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }

            private class MultiplexorHandler implements Multiplexor.Handler<AcceptorUdpChannel> {

                private final ByteBuffer readBuffer;

                private volatile SocketAddress remoteSocketAddress;

                private MultiplexorHandler() {
                    this.readBuffer =  ByteBuffer.allocate(4096); //TODO: size by packet
                }

                public AcceptorUdpChannel getChannel() {
                    return AcceptorUdpChannel.this;
                }

                public SocketAddress accept() {
                    return remoteSocketAddress;
                }

                public int receive(final SocketAddress socketAddress, final ByteBuffer buffer) {
                    if(buffer.hasRemaining() && socketAddress.equals(remoteSocketAddress)) {
                        final int offset = buffer.position();
                        buffer.put((ByteBuffer) readBuffer.flip());
                        return buffer.position() - offset;
                    }
                    return 0;
                }

                public int send(final SocketAddress socketAddress, final ByteBuffer buffer) {
                    try {
                        return datagramChannel.send(buffer, socketAddress);
                    }
                    catch(final IOException e) {
                        throw new IoException(e);
                    }
                }

                public void doOpen() {
                }

                public int doRead(final int length) {
                    try {
                        remoteSocketAddress = datagramChannel.receive(readBuffer);
                        return (remoteSocketAddress != null && doAccept(remoteSocketAddress))?
                               proxyMultiplexor.getMultiplexorHandler(remoteSocketAddress, Multiplexor.READ_OP).doRead(length) :
                               0;
                    }
                    catch(final IOException e) {
                        throw new IoException(e);
                    }
                    finally {
                        readBuffer.clear();
                        remoteSocketAddress = null;
                    }
                }

                public int doWrite(final int length) {
                    int remaining = length;
                    for(SocketAddress socketAddress = poll(); socketAddress != null && remaining > 0; socketAddress = poll()) {
                        remaining -= proxyMultiplexor.getMultiplexorHandler(socketAddress, Multiplexor.WRITE_OP).doWrite(length);
                    }
                    return length - remaining;
                }

                public void doClose() {
                    changeState(State.CLOSING);
                }
            }

            private class ProxyDatagramChannel extends DatagramChannel {

                private final SocketAddress remoteSocketAddress;

                private ProxyDatagramChannel(final SocketAddress remoteSocketAddress) {
                    super(SelectorProvider.provider());
                    this.remoteSocketAddress = remoteSocketAddress;
                }

                public SocketAddress getLocalAddress() throws IOException {
                    return datagramChannel.getLocalAddress();
                }

                public SocketAddress getRemoteAddress() throws IOException {
                    return remoteSocketAddress;
                }

                public <T> T getOption(final SocketOption<T> socketOption) throws IOException {
                    return datagramChannel.getOption(socketOption);
                }

                public Set<SocketOption<?>> supportedOptions() {
                    return datagramChannel.supportedOptions();
                }

                public <T> DatagramChannel setOption(final SocketOption<T> socketOption, final T value) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public DatagramSocket socket() {
                    return datagramChannel.socket();
                }

                public boolean isConnected() {
                    return datagramChannel.isConnected();
                }

                public DatagramChannel bind(final SocketAddress socketAddress) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public DatagramChannel connect(final SocketAddress socketAddress) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public DatagramChannel disconnect() throws IOException {
                    throw new UnsupportedOperationException();
                }

                public SocketAddress receive(final ByteBuffer buffer) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public int send(final ByteBuffer buffer, final SocketAddress socketAddress) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public int read(final ByteBuffer buffer) throws IOException {
                    return multiplexorHandler.receive(remoteSocketAddress, buffer);
                }

                public long read(final ByteBuffer[] buffers, final int offset, final int length) throws IOException {
                    return multiplexorHandler.receive(remoteSocketAddress, buffers[offset]);
                }

                public int write(final ByteBuffer buffer) throws IOException {
                    return multiplexorHandler.send(remoteSocketAddress, buffer);
                }

                public long write(final ByteBuffer[] buffers, final int offset, final int length) throws IOException {
                    long written = 0;
                    for(int i = offset; i < length; i++) written += multiplexorHandler.send(remoteSocketAddress, buffers[i]);
                    return written;
                }

                public MembershipKey join(final InetAddress group, final NetworkInterface networkInterface) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public MembershipKey join(final InetAddress group, final NetworkInterface networkInterface, final InetAddress inetAddress) throws IOException {
                    throw new UnsupportedOperationException();
                }

                protected void implCloseSelectableChannel() throws IOException {
                    doClose(remoteSocketAddress);
                }

                protected void implConfigureBlocking(final boolean block) throws IOException {
                }
            }
        }

        private class ProxyMultiplexor implements Multiplexor<NioChannel<DatagramChannel>>
        {
            private final Map<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>> readMultiplexorHandlerBySocketAddress;
            private final Map<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>> writeMultiplexorHandlerBySocketAddress;
            private final Map<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>> closeMultiplexorHandlerBySocketAddress;
            private final NullHandler nullMultiplexorHandler;

            private ProxyMultiplexor()
            {
                this.readMultiplexorHandlerBySocketAddress = new HashMap<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>>();
                this.writeMultiplexorHandlerBySocketAddress = new HashMap<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>>();
                this.closeMultiplexorHandlerBySocketAddress = new HashMap<SocketAddress, Handler<? extends NioChannel<DatagramChannel>>>();
                this.nullMultiplexorHandler = new NullHandler();
            }

            public Handler<? extends NioChannel<DatagramChannel>> getMultiplexorHandler(final SocketAddress socketAddress, final int ops)
            {
                final Handler<? extends NioChannel<DatagramChannel>> handler =
                        ((ops & READ_OP) != 0)? readMultiplexorHandlerBySocketAddress.get(socketAddress) :
                        ((ops & WRITE_OP) != 0)? writeMultiplexorHandlerBySocketAddress.get(socketAddress) :
                        ((ops & CLOSE_OP) != 0)? closeMultiplexorHandlerBySocketAddress.get(socketAddress) :
                        null;
                return (handler != null)? handler : nullMultiplexorHandler;
            }

            public void register(final Handler<? extends NioChannel<DatagramChannel>> handler,
                                 final int ops) {
                try {
                    final NioChannel<DatagramChannel> channel = handler.getChannel();
                    final SocketAddress socketAddress = channel.getChannel().getRemoteAddress();
                    if((ops & READ_OP) != 0) readMultiplexorHandlerBySocketAddress.put(socketAddress, handler);
                    if((ops & WRITE_OP) != 0) writeMultiplexorHandlerBySocketAddress.put(socketAddress, handler);
                    if((ops & CLOSE_OP) != 0) closeMultiplexorHandlerBySocketAddress.put(socketAddress, handler);
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }

            public void deregister(final Handler<? extends NioChannel<DatagramChannel>> handler,
                                   final int ops) {
                try {
                    final NioChannel<DatagramChannel> channel = handler.getChannel();
                    final SocketAddress socketAddress = channel.getChannel().getRemoteAddress();
                    if((ops & READ_OP) != 0) readMultiplexorHandlerBySocketAddress.remove(socketAddress);
                    if((ops & WRITE_OP) != 0) writeMultiplexorHandlerBySocketAddress.remove(socketAddress);
                    if((ops & CLOSE_OP) != 0) closeMultiplexorHandlerBySocketAddress.remove(socketAddress);
                }
                catch(final IOException e) {
                    throw new IoException(e);
                }
            }

            public void destroy() {
            }

            private class NullHandler implements Handler<NioChannel<DatagramChannel>>
            {

                public NioChannel<DatagramChannel> getChannel() {
                    throw new UnsupportedOperationException();
                }

                public void doOpen() {
                }

                public int doRead(final int length) {
                    return 0;
                }

                public int doWrite(final int length) {
                    return 0;
                }

                public void doClose() {
                }
            }
        }
    }
}
