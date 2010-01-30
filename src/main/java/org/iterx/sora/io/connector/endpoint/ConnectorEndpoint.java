package org.iterx.sora.io.connector.endpoint;

import org.iterx.sora.io.Uri;

public final class ConnectorEndpoint implements Endpoint {

    private final Uri uri;

    public ConnectorEndpoint(final Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
}
