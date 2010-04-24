package org.iterx.sora.io.connector.session.http.message;

public abstract class AbstractHttpMessage<T extends HttpMessage> implements HttpMessage<T> {

    public enum Version {
        HTTP_1_0("HTTP/1.0"),
        HTTP_1_1("HTTP/1.1");

        private final String name;

        private Version(final String name){
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }

    }
    
    protected interface GeneralHeaders {
        public static final Header<String> CACHE_CONTROL = new AbstractHeader<String>("Cache-Control", String.class){};
        public static final Header<String> CONNECTION = new AbstractHeader<String>("Connection", String.class){};
        public static final Header<String> DATE = new AbstractHeader<String>("Date", String.class){};
        public static final Header<String> PRAGMA = new AbstractHeader<String>("Pragma", String.class){};
        public static final Header<String> TRAILER = new AbstractHeader<String>("Trailer", String.class){};
        public static final Header<String> TRANSFER_ENCODING = new AbstractHeader<String>("Transfer-Encoding", String.class){};
        public static final Header<String> UPGRADE = new AbstractHeader<String>("Upgrade", String.class){};
        public static final Header<String> VIA = new AbstractHeader<String>("Via", String.class){};
        public static final Header<String> WARNING = new AbstractHeader<String>("Warning", String.class){};
    }

    protected interface EntityHeaders {
        public static final Header<String> ALLOW = new AbstractHeader<String>("Allow", String.class){};
        public static final Header<String> CONTENT_ENCODING = new AbstractHeader<String>("Content-Encoding", String.class){};
        public static final Header<String> CONTENT_LANGUAGE = new AbstractHeader<String>("Content-Language", String.class){};
        public static final Header<String> CONTENT_LENGTH = new AbstractHeader<String>("Content-Length", String.class){};
        public static final Header<String> CONTENT_LOCATION = new AbstractHeader<String>("Content-Location", String.class){};
        public static final Header<String> CONTENT_MD5 = new AbstractHeader<String>("Content-MD5", String.class){};
        public static final Header<String> CONTENT_RANGE = new AbstractHeader<String>("Content-Range", String.class){};
        public static final Header<String> CONTENT_TYPE = new AbstractHeader<String>("Content-Type", String.class){};
        public static final Header<String> EXPIRES = new AbstractHeader<String>("Expires", String.class){};
        public static final Header<String> LAST_MODIFIED = new AbstractHeader<String>("Last-Modified", String.class){};
    }


    protected static abstract class AbstractHeader<T> implements Header<T> {

        private final String name;
        private final Class<T> type;

        protected AbstractHeader(final String name, final Class<T> type) {
            this.name = name;
            this.type = type;
        }

        public String name() {
            return name;
        }

        public Class<T> type() {
            return type;
        }

        @Override
        public String toString() {
            return name;
        }
    }

}
