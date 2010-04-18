package org.iterx.sora.io.connector.session.http.message;

import org.iterx.sora.io.Uri;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class HttpRequest implements HttpMessage<HttpRequest> {

    public enum Method {
        GET,
        POST,
        HEAD
    }

    private final Uri uri;

    //private final String httpVersion;

    private HttpRequest(final Uri uri) {
        this.uri = uri;
    }

    public static HttpRequest newHttpRequest(final Uri uri) {
        return new HttpRequest(uri);
    }

    //TODO: set headers & properties

    //TODO: implement internal marshaller -> to ByteBuffer


    public void encode(final DataOutput dataOutput) throws IOException {
        dataOutput.writeBytes("GET " + uri.getPath() + " HTTP/1.0\n\n");
    }

    public void decode(final DataInput dataInput) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        for(char c = (char) dataInput.readByte(); c != '\n'; c = (char) dataInput.readByte()) {
            stringBuilder.append(c);
        }
        dataInput.readByte(); //read in last '\n'
    }
}
