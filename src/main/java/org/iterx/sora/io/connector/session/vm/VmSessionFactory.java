package org.iterx.sora.io.connector.session.vm;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.multiplexor.Multiplexor;
import org.iterx.sora.io.connector.session.Channel;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionFactory;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public final class VmSessionFactory implements SessionFactory<VmSession, VmSession, ByteBuffer, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("vm://([^#]*)?(#.*)?");

    private final Multiplexor<? super Channel<ByteBuffer, ByteBuffer>> multiplexor;

    public VmSessionFactory(final Multiplexor<? super Channel<ByteBuffer, ByteBuffer>> multiplexor) {
        this.multiplexor = multiplexor;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public VmSession newSession(final Connector connector,
                                final Session.SessionCallback<? super VmSession> sessionCallback,
                                final AcceptorEndpoint acceptorEndpoint) {
        assertEndpoint(acceptorEndpoint);
        return new VmSession(multiplexor, sessionCallback, acceptorEndpoint);
    }

    public VmSession newSession(final Connector connector,
                                final Session.SessionCallback<? super VmSession> sessionCallback,
                                final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new VmSession(multiplexor, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}