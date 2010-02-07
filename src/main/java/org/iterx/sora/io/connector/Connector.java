package org.iterx.sora.io.connector;

import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionFactory;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.collection.List;
import org.iterx.sora.collection.list.LinkedList;

public final class Connector {

    private final List<SessionFactory<? extends Session<?, ?>, ?>> sessionFactories;

    public Connector(final SessionFactory<? extends Session<?, ?>, ?>... sessionFactories) {
        this.sessionFactories = copyOf(sessionFactories);
    }

    public <S extends Session<?, ?>> S newSession(final Session.Callback<? super S> sessionCallback, final AcceptorEndpoint acceptorEndpoint) {
        final SessionFactory<S, ?> sessionFactory = resolve(acceptorEndpoint);
        return sessionFactory.newSession(this, sessionCallback, acceptorEndpoint);
    }

    public <S extends Session<?, ?>> S newSession(final Session.Callback<? super S> sessionCallback, final ConnectorEndpoint connectorEndpoint) {
        final SessionFactory<S, ?> sessionFactory = resolve(connectorEndpoint);
        return sessionFactory.newSession(this, sessionCallback, connectorEndpoint);
    }

    @SuppressWarnings("unchecked")
    private <S extends Session<?, ?>> SessionFactory<S, ?> resolve(final Endpoint endpoint) {
        for(final SessionFactory<? extends Session, ?> sessionFactory : sessionFactories) {
            if(sessionFactory.supports(endpoint)) return (SessionFactory<S, ?>) sessionFactory;
        }
        throw new IllegalArgumentException("Unsupported endpoint '" + endpoint + "'");
    }

    private List<SessionFactory<? extends Session<?, ?>, ?>> copyOf(final SessionFactory<? extends Session<?, ?>, ?>... sessionFactories) {
        final List<SessionFactory<? extends Session<?, ?>, ?>> copy =  new LinkedList<SessionFactory<? extends Session<?, ?>, ?>>();
        for(final SessionFactory<? extends Session<?, ?>, ?> sessionFactory : sessionFactories) copy.add(sessionFactory);
        return copy;
    }
}
