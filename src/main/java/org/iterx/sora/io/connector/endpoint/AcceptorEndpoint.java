package org.iterx.sora.io.connector.endpoint;

import org.iterx.sora.io.Uri;

public final class AcceptorEndpoint implements Endpoint {

    private final Uri uri;

    public AcceptorEndpoint(final Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }
        
    @Override
    public String toString() {
        return Helper.toString(this);
    }
}
