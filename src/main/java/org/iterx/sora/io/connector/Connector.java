package org.iterx.sora.io.connector;

import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionProvider;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.collection.List;
import org.iterx.sora.collection.list.LinkedList;

public final class Connector {

    private final List<SessionProvider<? extends Session<?, ?>, ?>> sessionProviders;

    public Connector(final SessionProvider<? extends Session<?, ?>, ?>... sessionProviders) {
        this.sessionProviders = copyOf(sessionProviders);
    }

    public <S extends Session<?, ?>> S newSession(final Session.Callback<? super S> sessionCallback, final AcceptorEndpoint acceptorEndpoint) {
        final SessionProvider<S, ?> sessionProvider = resolve(acceptorEndpoint);
        return sessionProvider.newSession(this, sessionCallback, acceptorEndpoint);
    }

    public <S extends Session<?, ?>> S newSession(final Session.Callback<? super S> sessionCallback, final ConnectorEndpoint connectorEndpoint) {
        final SessionProvider<S, ?> sessionProvider = resolve(connectorEndpoint);
        return sessionProvider.newSession(this, sessionCallback, connectorEndpoint);
    }

    @SuppressWarnings("unchecked")
    private <S extends Session<?, ?>> SessionProvider<S, ?> resolve(final Endpoint endpoint) {
        for(final SessionProvider<? extends Session, ?> sessionProvider : sessionProviders) {
            if(sessionProvider.supports(endpoint)) return (SessionProvider<S, ?>) sessionProvider;
        }
        throw new IllegalArgumentException("Unsupported endpoint '" + endpoint + "'");
    }

    private List<SessionProvider<? extends Session<?, ?>, ?>> copyOf(final SessionProvider<? extends Session<?, ?>, ?>... sessionProviders) {
        final List<SessionProvider<? extends Session<?, ?>, ?>> copy =  new LinkedList<SessionProvider<? extends Session<?, ?>, ?>>();
        for(final SessionProvider<? extends Session<?, ?>, ?> sessionProvider : sessionProviders) copy.add(sessionProvider);
        return copy;
    }
}
