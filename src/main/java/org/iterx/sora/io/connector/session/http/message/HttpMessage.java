package org.iterx.sora.io.connector.session.http.message;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;


public interface HttpMessage<T extends HttpMessage> {

    //TODO: should make inner class & pass in HttpMessage object 
    void encode(DataOutput dataOutput) throws IOException;

    void decode(DataInput dataInput) throws IOException;

}
