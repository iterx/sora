package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionProvider;
import org.iterx.sora.io.connector.support.nio.strategy.MultiplexorStrategy;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SocketChannel;
import java.util.regex.Pattern;

public final class TcpSessionProvider implements SessionProvider<TcpSession, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("tcp://(/[^#]*)?(#.*)?");

    private final MultiplexorStrategy<? super SelectableChannel> multiplexorStrategy;

    public TcpSessionProvider(final MultiplexorStrategy<? super SelectableChannel> multiplexorStrategy) {
        this.multiplexorStrategy = multiplexorStrategy;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public TcpSession newSession(final Connector connector,
                                 final Session.Callback<? super TcpSession> sessionCallback,
                                 final AcceptorEndpoint acceptorEndpoint) {
        assertEndpoint(acceptorEndpoint);
        return new TcpSession(multiplexorStrategy, sessionCallback, acceptorEndpoint);
    }

    public TcpSession newSession(final Connector connector,
                                 final Session.Callback<? super TcpSession> sessionCallback,
                                 final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new TcpSession(multiplexorStrategy, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}