package org.iterx.sora.io.connector.session.http.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

public class HttpResponse implements HttpMessage<HttpResponse> {

    //TODO: Convert to class -> allow customisation of serialization  

    public enum Status {
        OK,
        SERVER_ERROR
    }

    private HttpResponse() {
    }
            
    public static HttpResponse newHttpResponse(final Status status)  {
        return new HttpResponse();
    }

    public void encode(final DataOutput dataOutput) throws IOException{
        dataOutput.writeBytes("200 OK\n\n");
    }

    public void decode(final DataInput dataInput) throws IOException {
        final StringBuilder stringBuilder = new StringBuilder();
        for(char c = (char) dataInput.readByte(); c != '\n'; c = (char) dataInput.readByte()) {
            stringBuilder.append(c);
        }
        dataInput.readByte(); //read in last '\n'
    }
}
