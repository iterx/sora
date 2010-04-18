package org.iterx.sora.io.connector.session.http.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.nio.ByteBuffer;

public class HttpResponse implements HttpMessage<HttpResponse> {

    //TODO: Convert to class -> allow customisation of serialization  

    public enum Status {
        OK,
        SERVER_ERROR
    }


    public void encode(final DataOutput dataOutput) {
        throw new UnsupportedOperationException();
    }

    public void decode(final DataInput dataInput) {
        throw new UnsupportedOperationException();
    }
}
