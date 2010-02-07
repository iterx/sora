package org.iterx.sora.io.connector.session;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;

public interface SessionFactory<S extends Session<?, T>, T> {

    boolean supports(Endpoint endpoint);

    S newSession(Connector connector,
                 Session.Callback<? super S> sessionCallback,
                 AcceptorEndpoint acceptorEndpoint);

    S newSession(Connector connector,
                 Session.Callback<? super S> sessionCallback, 
                 ConnectorEndpoint connectorEndpoint);
}
