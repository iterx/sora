package org.iterx.sora.io.connector.session;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;


public interface SessionFactory<S extends Session<?, R, W>, T extends Session<?, W, R>,  R, W> {

    boolean supports(Endpoint endpoint);

    S newSession(Connector connector,
                 Session.SessionCallback<? super S> sessionCallback,
                 AcceptorEndpoint acceptorEndpoint);

    T newSession(Connector connector,
                 Session.SessionCallback<? super T> sessionCallback,
                 ConnectorEndpoint connectorEndpoint);
}
