package org.iterx.sora.io.connector.support.nio.session.tcp;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.Multiplexor;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionProvider;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public final class TcpSessionProvider implements SessionProvider<TcpSession, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("tcp://([^#]*)?(#.*)?");

    private final Multiplexor<? super NioChannel> multiplexor;

    public TcpSessionProvider(final Multiplexor<? super NioChannel> multiplexor) {
        this.multiplexor = multiplexor;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public TcpSession newSession(final Connector connector,
                                 final Session.Callback<? super TcpSession> sessionCallback,
                                 final AcceptorEndpoint acceptorEndpoint) {
        assertEndpoint(acceptorEndpoint);
        return new TcpSession(multiplexor, sessionCallback, acceptorEndpoint);
    }

    public TcpSession newSession(final Connector connector,
                                 final Session.Callback<? super TcpSession> sessionCallback,
                                 final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new TcpSession(multiplexor, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}