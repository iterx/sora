package org.iterx.sora.io.connector.support.nio.session.udp;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionProvider;
import org.iterx.sora.io.connector.support.nio.strategy.MultiplexorStrategy;

import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.util.regex.Pattern;

public final class UdpSessionProvider implements SessionProvider<UdpSession, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("udp://(/[^#]*)?(#.*)?");

    private final MultiplexorStrategy<? super DatagramChannel> multiplexorStrategy;

    public UdpSessionProvider(final MultiplexorStrategy<? super DatagramChannel> multiplexorStrategy) {
        this.multiplexorStrategy = multiplexorStrategy;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public UdpSession newSession(final Connector connector,
                                 final Session.Callback<? super UdpSession> sessionCallback,
                                 final AcceptorEndpoint acceptorEndpoint) {
        assertEndpoint(acceptorEndpoint);
        return new UdpSession(multiplexorStrategy, sessionCallback, acceptorEndpoint);
    }

    public UdpSession newSession(final Connector connector,
                                 final Session.Callback<? super UdpSession> sessionCallback,
                                 final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new UdpSession(multiplexorStrategy, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}
