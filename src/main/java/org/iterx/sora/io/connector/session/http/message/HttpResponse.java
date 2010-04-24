package org.iterx.sora.io.connector.session.http.message;

import org.iterx.sora.collection.Arrays;
import org.iterx.sora.collection.Map;
import org.iterx.sora.collection.map.HashMap;
import org.iterx.sora.io.Uri;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import static org.iterx.sora.util.Exception.rethrow;

public class HttpResponse extends AbstractHttpMessage<HttpResponse> {

    public enum Status {
        CONTINUE(100, "Continue"),
        SWITCHING_PROTOCOLS(101, "Switching Protocols"),
        OK(200, "OK"),
        CREATED(201, "Created"),
        ACCEPTED(202, "Accepted"),
        NON_AUTHORITATIVE_INFORMATION(203, "Non-Authoritative Information"),
        NO_CONTENT(204, "No Content"),
        RESET_CONTENT(205, "Reset Content"),
        PARTIAL_CONTENT(206, "Partial Content"),
        MULTIPLE_CHOICES(300, "Multiple Choices"),
        MOVED_PERMANENTLY(301, "Moved Permanently"),
        FOUND(302, "Found"),
        SEE_OTHER(303, "See Other"),
        NOT_MODIFIED(304, "Not Modified"),
        USE_PROXY(305, "Use Proxy"),
        TEMPORARY_REDIRECT(307, "Temporary Redirect"),
        BAD_REQUEST(400, "Bad Request"),
        UNAUTHORIZED(401, "Unauthorized"),
        PAYMENT_REQUIRED(402, "Payment Required"),
        FORBIDDEN(403, "Forbidden"),
        NOT_FOUND(404, "Not Found"),
        METHOD_NOT_ALLOWED(405, "Method Not Allowed"),
        NOT_ACCEPTABLE(406, "Not Acceptable"),
        PROXY_AUTHENTICATION_REQUIRED(407, "Proxy Authentication Required"),     
        REQUEST_TIME_OUT(408, "Request Time-out"),      
        CONFLICT(409, "Conflict"),         
        GONE(410, "Gone"),        
        LENGTH_REQUIRED(411, "Length Required"),     
        PRECONDITION_FAILED(412, "Precondition Failed"),  
        REQUEST_ENTITY_TOO_LARGE(413, "Request Entity Too Large"), 
        REQUEST_URI_TOO_LARGE(414, "Request-URI Too Large"),    
        UNSUPPORTED_MEDIA_TYPE(415, "Unsupported Media Type"),    
        REQUEST_RANGE_NOT_SATISFIABLE(416, "Requested range not satisfiable"), 
        EXPECTATION_FAILED(417, "Expectation Failed"),        
        INTERNAL_SERVER_ERROR(500, "Internal Server Error"),     
        NOT_IMPLEMENTED(501, "Not Implemented"),       
        BAD_GATEWAY(502, "Bad Gateway"),        
        SERVICE_UNAVAILABLE(503, "Service Unavailable"),    
        GATEWAY_TIME_OUT(504, "Gateway Time-out"),        
        HTTP_VERSION_NOT_SUPPORTED(505, "HTTP Version not supported");

        private final int code;
        private final String name;

        private Status(final int code, final String name) {
            this.code = code;
            this.name = name;
        }

        public int code() {
            return code;
        }
        
        @Override
        public String toString() {
            return name;
        }

        public static Status valueOf(final int code) {
            for(final Status status : values()) if(status.code() == code) return status;
            throw new IllegalArgumentException("Invalid code '" + code + "'");
        }
    }

    public static class ResponseHeaders implements GeneralHeaders, EntityHeaders {

        public static final Header<String> ACCEPT_RANGES = new AbstractHeader<String>("Accept-Ranges", String.class){};
        public static final Header<String> AGE = new AbstractHeader<String>("Age", String.class){};
        public static final Header<String> ETAG = new AbstractHeader<String>("ETag", String.class){};
        public static final Header<String> LOCATION = new AbstractHeader<String>("Location", String.class){};
        public static final Header<String> PROXY_AUTHENTICATE = new AbstractHeader<String>("Proxy-Authenticate", String.class){};
        public static final Header<String> RETRY_AFTER = new AbstractHeader<String>("Retry-After", String.class){};
        public static final Header<String> SERVER = new AbstractHeader<String>("Server", String.class){};
        public static final Header<String> VARY = new AbstractHeader<String>("Vary", String.class){};
        public static final Header<String> WWW_AUTHENTICATE = new AbstractHeader<String>("WWW-Authenticate", String.class){};

        private static final Map<String, Header> HEADERS_BY_NAME = new HashMap<String, Header>();
        static {
            try {
                for(final Field field : ResponseHeaders.class.getFields()) {
                    if(Header.class.isAssignableFrom(field.getType())) {
                        final Header<?> header = (Header<?>) field.get(ResponseHeaders.class);
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
            final T header = (T) HEADERS_BY_NAME.get(name);
            if(header != null) return header;
            throw new IllegalArgumentException("Invalid header '" + name + "'");
        }
    }

    private Status status;
    private Version version;
    private Map<Header<?>, Object[]> headers;


    private HttpResponse(final Status status, final Version version) {
        this.headers = new HashMap<Header<?>, Object[]>();
        this.status = status;
        this.version = version;
    }

    public static HttpResponse newHttpResponse() {
        return new HttpResponse(Status.OK, Version.HTTP_1_1);
    }

    public static HttpResponse newHttpResponse(final Status status)  {
        return newHttpResponse(status, Version.HTTP_1_1);
    }

    //TODO: Build from request????
    public static HttpResponse newHttpResponse(final Status status, final Version version)  {
        return new HttpResponse(status, version);
    }

    public Status getStatus() {
        return status;
    }

    public Version getVersion() {
        return version;
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getHeaders(final Header<T> header) {
        return (headers.containsKey(header))? (T[]) headers.get(header) : (T[]) Arrays.newArray(header.type(), 0);
    }

    public ByteBuffer getBody(final ByteBuffer buffer) {

        //TODO: how do we cope with multiple parts! -> should be by byte[]
        //TODO: should we force copy????
        //TODO: should we enable registration of smarthandlers/overrides to automatically pass this into nice data structure

        //TODO: ***********
        //TODO: or do we use callbacks -> to request body when streaming????
        return buffer;
    }

    public HttpResponse clear() {
        headers.clear();
        status = Status.OK;
        version = Version.HTTP_1_1;
        return this;
    }

    /////////////////////////////////////
    //TODO: Break out as builders, marshallers & validators!!!!

    public <T> HttpResponse addHeader(final Header<T> header, final T value) { //TODO: should be RequestHeader
        //TODO: Fix typing on headers -> allow for extension
        if(headers.containsKey(header)) headers.put(header, Arrays.add(headers.get(header), value));
        else headers.put(header, Arrays.newArray(header.type(), value));
        return this;
    }

    public <T> HttpResponse removeHeader(final Header<T> header, final T value) {
        if(headers.containsKey(header)) {
            final Object[] values = Arrays.remove(headers.get(header), value);
            if(values.length != 0) headers.put(header, values);
            else headers.remove(header);
        }
        return this;
    }

    public HttpResponse setStatus(final Status status) {
        this.status = status;
        return this;
    }

    public HttpResponse setBody(final ByteBuffer buffer) {
        //TODO: does this want to be bytebuffers???
        throw new UnsupportedOperationException();
    }

    private static final String CR_LF = "\r\n";
    private static final String HEADER_SEPARATOR = ":";
    private static final String SP = " ";

    public void encode(final DataOutput dataOutput) throws IOException{
        //Status-Line
        dataOutput.writeBytes(version.toString());
        dataOutput.writeBytes(SP);
        dataOutput.writeBytes(String.valueOf(status.code()));
        dataOutput.writeBytes(SP);
        dataOutput.writeBytes(status.toString());
        dataOutput.writeBytes(CR_LF);
        //Response-Headers
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
        //Response-Body
        //TODO: TBI
    }

    public void decode(final DataInput dataInput) throws IOException {
        //Status-Line
        readBytes(dataInput, SP); version = Version.HTTP_1_1; //TODO: Fix lookup
        status = Status.valueOf(Integer.parseInt(readBytes(dataInput, SP)));
        readBytes(dataInput, CR_LF); //

        //Response-Headers
        for(String header = readBytes(dataInput, CR_LF); header.length() != 0; header = readBytes(dataInput, CR_LF)) {
            final int index = header.indexOf(HEADER_SEPARATOR);
            if(index != -1) {
                final String name = header.substring(0, index);
                final String value = (header.length() > index)? header.substring(index + 1).trim() : null;
                addHeader(ResponseHeaders.<Header<String>>valueOf(name), value);
                continue;
            }
            throw new IOException("Malformed HttpRequest");
        }
        //Response-Body
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
