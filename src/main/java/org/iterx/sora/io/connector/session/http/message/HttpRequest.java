package org.iterx.sora.io.connector.session.http.message;

import org.iterx.sora.collection.Arrays;
import org.iterx.sora.collection.Map;
import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.io.Uri;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.iterx.sora.util.Exception.rethrow;

public class HttpRequest extends AbstractHttpMessage<HttpRequest> {

    public enum Method {
        OPTIONS,
        GET,
        HEAD,
        POST,
        PUT,
        DELETE,
        TRACE,
        CONNECT
    }

    public static class RequestHeaders implements GeneralHeaders, EntityHeaders {
        public static final Header<String> ACCEPT = new AbstractHeader<String>("Accept", String.class){};
        public static final Header<String> ACCEPT_CHARSET = new AbstractHeader<String>("Accept-Charset", String.class){};
        public static final Header<String> ACCEPT_ENCODING = new AbstractHeader<String>("Accept-Encoding", String.class){};
        public static final Header<String> ACCEPT_LANGUAGE = new AbstractHeader<String>("Accept-Language", String.class){};
        public static final Header<String> AUTHORIZATION = new AbstractHeader<String>("Authorization", String.class){};
        public static final Header<String> EXPECT = new AbstractHeader<String>("Expect", String.class){};
        public static final Header<String> FROM = new AbstractHeader<String>("From", String.class){};
        public static final Header<String> HOST = new AbstractHeader<String>("Host", String.class){};
        public static final Header<String> IF_MATCH = new AbstractHeader<String>("If-Match", String.class){};
        public static final Header<String> IF_MODIFIED_SINCE = new AbstractHeader<String>("If-Modified-Since", String.class){};
        public static final Header<String> IF_NONE_MATCH = new AbstractHeader<String>("If-None-Match", String.class){};
        public static final Header<String> IF_RANGE = new AbstractHeader<String>("If-Range", String.class){};
        public static final Header<String> IF_UNMODIFIED_SINCE = new AbstractHeader<String>("If-Unmodified-Since", String.class){};
        public static final Header<String> MAX_FORWARDS = new AbstractHeader<String>("Max-Forwards", String.class){};
        public static final Header<String> PROXY_AUTHORIZATION = new AbstractHeader<String>("Proxy-Authorization", String.class){};
        public static final Header<String> RANGE = new AbstractHeader<String>("Range", String.class){};
        public static final Header<String> REFERER = new AbstractHeader<String>("Referer", String.class){};
        public static final Header<String> TE = new AbstractHeader<String>("TE", String.class){};
        public static final Header<String> USER_AGENT = new AbstractHeader<String>("User-Agent", String.class){};

        private static final Map<String, Header> HEADERS_BY_NAME = new HashMap<String, Header>();

        static {
            try {
                for(final Field field : RequestHeaders.class.getFields()) {
                    if(Header.class.isAssignableFrom(field.getType())) {
                        final Header<?> header = (Header<?>) field.get(RequestHeaders.class);
                        HEADERS_BY_NAME.put(header.name(), header);
                    }
                }
            }
            catch(final Exception e) {
                throw rethrow(e);
            }
        }

        @SuppressWarnings("unchecked")
        public static <T extends Header> T valueOf(final String name) {
            final T header = (T) HEADERS_BY_NAME.get(name); //TODO: need to normalize names
            if(header != null) return header;
            throw new IllegalArgumentException("Invalid header '" + name + "'");
        }
    }

    private Version version;
    private Method method;
    private Uri uri;

    private Map<Header<?>, Object[]> headers;

    private HttpRequest(final Method method,
                        final Uri uri,
                        final Version version) {
        this.headers = new HashMap<Header<?>, Object[]>();
        this.method = method;
        this.uri = uri;
        this.version = version;
    }

    public static HttpRequest newHttpRequest() {
        return new HttpRequest(null, null, Version.HTTP_1_1);
    }

    public static HttpRequest newHttpRequest(final Method method, final Uri uri) {
        return newHttpRequest(method, uri, Version.HTTP_1_1);
    }

    public static HttpRequest newHttpRequest(final Method method, final Uri uri, final Version version) {
        final HttpRequest httpRequest = new HttpRequest(method, uri, version);
        httpRequest.addHeader(RequestHeaders.HOST, uri.getHost()); //TODO: Fix this -> need to collapse uri handling
        return httpRequest;
    }

    public Version getVersion() {
        return version;
    }

    public Method getMethod() {
        return method;
    }

    public Uri getUri() {
        return uri;
    }


    @SuppressWarnings("unchecked")
    public <T> T[] getHeaders(final Header<T> header) {
        return (headers.containsKey(header))? (T[]) headers.get(header) : (T[]) Arrays.newArray(header.type(), 0);
    }

    public ByteBuffer getBody(final ByteBuffer buffer) {
        //TODO: how do we cope with multiple parts! -> should be by byte[]
        //TODO: should we force copy????
        //TODO: should we enable registration of smarthandlers/overrides to automatically pass this into nice data structure
        return buffer;
    }

    public HttpRequest clear() {
        headers.clear();
        method = null;
        uri = null;
        version = Version.HTTP_1_1;
        return this;
    }

    /////////////////////////////////////
    //TODO: Break out as builders, marshallers & validators!!!!

    public <T> HttpRequest addHeader(final Header<T> header, final T value) { //TODO: should be RequestHeader
        //TODO: Fix typing on headers -> allow for extension
        if(headers.containsKey(header)) headers.put(header, Arrays.add(headers.get(header), value));
        else headers.put(header, Arrays.newArray(header.type(), value));
        return this;
    }


    public <T> HttpRequest removeHeader(final Header<T> header, final T value) {
        if(headers.containsKey(header)) {
            final Object[] values = Arrays.remove(headers.get(header), value);
            if(values.length != 0) headers.put(header, values);
            else headers.remove(header);
        }
        return this;
    }

    public HttpRequest setBody(final ByteBuffer buffer) {
        //TODO: does this want to be bytebuffers???
        throw new UnsupportedOperationException();
    }

    private static final String CR_LF = "\r\n";
    private static final String HEADER_SEPARATOR = ":";
    private static final String SP = " ";

    public void encode(final DataOutput dataOutput) throws IOException {
        //Request-Line
        dataOutput.writeBytes(method.toString());
        dataOutput.writeBytes(SP);
        dataOutput.writeBytes(uri.getPath()); //TODO: Fix this -> should intelligently handle this -> mangle host/path
        dataOutput.writeBytes(SP);
        dataOutput.writeBytes(version.toString());
        dataOutput.writeBytes(CR_LF);

        //Request-Headers
        for(final Map.Entry<Header<?>, Object[]> entry : headers.entrySet()) {
            final Header<?> header = entry.getKey();
            for(final Object value : entry.getValue()) {
                dataOutput.writeBytes(header.name());
                dataOutput.writeBytes(HEADER_SEPARATOR);
                dataOutput.writeBytes(SP);
                dataOutput.writeBytes(value.toString()); //TODO: Fix encoding
                dataOutput.writeBytes(CR_LF);
            }
        }
        dataOutput.writeBytes(CR_LF);
        //Request-Body
        //TODO: TBI
    }

    public void decode(final DataInput dataInput) throws IOException {
        //Request-Line
        method = Method.valueOf(readBytes(dataInput, SP));
        uri = new Uri(readBytes(dataInput, SP));
        readBytes(dataInput, CR_LF); version = Version.HTTP_1_1; //TODO: Fix lookup

        //Request-Headers
        for(String header = readBytes(dataInput, CR_LF); header.length() != 0; header = readBytes(dataInput, CR_LF)) {
            final int index = header.indexOf(HEADER_SEPARATOR);
            if(index != -1) {
                final String name = header.substring(0, index);
                final String value = (header.length() > index)? header.substring(index + 1).trim() : null;
                addHeader(RequestHeaders.<Header<String>>valueOf(name), value);
                continue;
            }
            throw new IOException("Malformed HttpRequest");
        }
        //Request-Body
        //TODO: TBI
    }

    private String readBytes(final DataInput dataInput, final String token) throws IOException{
        final StringBuilder stringBuilder = new StringBuilder();
        for(char c = (char) dataInput.readByte(); ; c = (char) dataInput.readByte()) {
            if(token.charAt(0) == c) {
                for(int i = 1, length = token.length(); i < length; i++)
                    if(token.charAt(i) != dataInput.readByte()) throw new IOException();
                return stringBuilder.toString();
            }
            stringBuilder.append(c);
        }
    }
}
