package org.iterx.sora.io.connector.session.http;

import org.iterx.sora.io.connector.Connector;
import org.iterx.sora.io.connector.endpoint.AcceptorEndpoint;
import org.iterx.sora.io.connector.endpoint.ConnectorEndpoint;
import org.iterx.sora.io.connector.endpoint.Endpoint;
import org.iterx.sora.io.connector.session.Session;
import org.iterx.sora.io.connector.session.SessionFactory;
import org.iterx.sora.io.connector.session.http.message.HttpRequest;
import org.iterx.sora.io.connector.session.http.message.HttpResponse;

import java.util.regex.Pattern;

public final class HttpSessionFactory implements SessionFactory<HttpSession<HttpRequest, HttpResponse>, HttpSession<HttpResponse, HttpRequest>, HttpRequest, HttpResponse> {

    private static final Pattern URI_PATTERN = Pattern.compile("http(s)?://(\\w+(:\\d+)?)(/[^#]*)?(#.*)?");

    public boolean supports(final Endpoint endpoint) {
        return URI_PATTERN.matcher(endpoint.getUri().toString()).matches();
    }

    public HttpSession<HttpRequest, HttpResponse> newSession(final Connector connector,
                                                             final Session.SessionCallback<? super HttpSession<HttpRequest, HttpResponse>> sessionCallback,
                                                             final AcceptorEndpoint acceptorEndpoint) {
        return new HttpSession<HttpRequest, HttpResponse>(connector, sessionCallback, acceptorEndpoint);
    }

    public HttpSession<HttpResponse, HttpRequest> newSession(final Connector connector,
                                                             final Session.SessionCallback<? super HttpSession<HttpResponse, HttpRequest>> sessionCallback,
                                                             final ConnectorEndpoint connectorEndpoint) {
        assertEndpoint(connectorEndpoint);
        return new HttpSession<HttpResponse, HttpRequest>(connector, sessionCallback, connectorEndpoint);
    }

    private void assertEndpoint(final Endpoint endpoint) {
        if(!supports(endpoint)) throw new IllegalArgumentException("Invalid endpoint '" + endpoint + "'");
    }

}
