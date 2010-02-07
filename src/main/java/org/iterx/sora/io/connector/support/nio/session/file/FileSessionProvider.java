package org.iterx.sora.io.connector.support.nio.session.file;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.Multiplexor;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionProvider;

import java.nio.ByteBuffer;
import java.util.regex.Pattern;

public final class FileSessionProvider implements SessionProvider<FileSession, ByteBuffer> {

    private static final Pattern URI_PATTERN = Pattern.compile("(file:)?//(/[^#]*)?(#.*)?");

    private final Multiplexor<? super FileChannel> multiplexor;

    public FileSessionProvider(final Multiplexor<? super FileChannel> multiplexor) {
        this.multiplexor = multiplexor;
    }

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public FileSession newSession(final Connector connector,
                                  final Session.Callback<? super FileSession> sessionCallback,
                                  final AcceptorEndpoint acceptorEndpoint) {
        throw new UnsupportedOperationException();
    }

    public FileSession newSession(final Connector connector,
                                  final Session.Callback<? super FileSession> sessionCallback,
                                  final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new FileSession(multiplexor, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }
}