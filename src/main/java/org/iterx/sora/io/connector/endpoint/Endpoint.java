package org.iterx.sora.io.connector.endpoint;

import org.iterx.sora.io.Uri;

public interface Endpoint {

    Uri getUri();

    static class Helper {

        public static String toString(final Endpoint endpoint) {
            return new StringBuilder().
                    append(endpoint.getClass().getSimpleName()).
                    append("[uri='").append(endpoint.getUri()).append("']").toString();
        }
    }
}
