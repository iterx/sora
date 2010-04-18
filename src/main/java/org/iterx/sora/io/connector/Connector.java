package org.iterx.sora.io.connector;

import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionFactory;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.collection.List;
import org.iterx.sora.collection.list.LinkedList;

public final class Connector {

    private final List<SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>> sessionFactories;

    public Connector(final SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>... sessionFactories) {
        this.sessionFactories = copyOf(sessionFactories);
    }

    public <S extends Session<?, ?, ?>> S newSession(final Session.SessionCallback<? super S> sessionCallback, final AcceptorEndpoint acceptorEndpoint) {
        final SessionFactory<S, ?, ?, ?> sessionFactory = resolve(acceptorEndpoint);
        return sessionFactory.newSession(this, sessionCallback, acceptorEndpoint);
    }

    public <T extends Session<?, ?, ?>> T newSession(final Session.SessionCallback<? super T> sessionCallback, final ConnectorEndpoint connectorEndpoint) {
        final SessionFactory<?, T, ?, ?> sessionFactory = resolve(connectorEndpoint);
        return sessionFactory.newSession(this, sessionCallback, connectorEndpoint);
    }

    @SuppressWarnings("unchecked")
    private <S extends Session<?, ?, ?>, T extends Session<?, ?, ?>> SessionFactory<S, T, ?, ?> resolve(final Endpoint endpoint) {
        for(final SessionFactory<? extends Session, ? extends Session, ?, ?> sessionFactory : sessionFactories) {
            if(sessionFactory.supports(endpoint)) return (SessionFactory<S, T, ?, ?>) sessionFactory;
        }
        throw new IllegalArgumentException("Unsupported endpoint '" + endpoint + "'");
    }

    private List<SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>> copyOf(final SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>... sessionFactories) {
        final List<SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>> copy =  new LinkedList<SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?>>();
        for(final SessionFactory<? extends Session<?, ?, ?>, ? extends Session<?, ?, ?>, ?, ?> sessionFactory : sessionFactories) copy.add(sessionFactory);
        return copy;
    }
}
