package org.iterx.sora.io.connector.support.nio.session.udp;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionFactory;
import org.iterx.sora.io.connector.support.nio.session.NioChannel;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public final class UdpSessionFactory implements SessionFactory<UdpSession, UdpSession, ByteBuffer, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("udp://([^#]*)?(#.*)?");

    private final Multiplexor<? super NioChannel> multiplexor;

    public UdpSessionFactory(final Multiplexor<? super NioChannel> multiplexor) {
        this.multiplexor = multiplexor;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public UdpSession newSession(final Connector connector,
                                 final Session.SessionCallback<? super UdpSession> sessionCallback,
                                 final AcceptorEndpoint acceptorEndpoint) {
        assertEndpoint(acceptorEndpoint);
        return new UdpSession(multiplexor, sessionCallback, acceptorEndpoint);
    }

    public UdpSession newSession(final Connector connector,
                                 final Session.SessionCallback<? super UdpSession> sessionCallback,
                                 final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new UdpSession(multiplexor, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}
