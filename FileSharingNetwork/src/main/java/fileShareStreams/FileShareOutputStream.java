package main.java.fileShareStreams;


import java.io.ObjectOutput;
import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import main.java.messages.*;
import main.java.log.*;


public class FileShareOutputStream extends DataOutputStream implements ObjectOutput {
    public FileShareOutputStream(OutputStream out) {
        super(out);
    }

    @Override
    public void writeObject (Object obj) throws IOException {
    	if(obj == null) return;

        if (obj instanceof Handshake) {
            ((Handshake) obj).write(this);
        } else if (obj instanceof Message) {
            ((Message) obj).write (this);
        } else if (obj instanceof FileShareStream) {
            throw new UnsupportedOperationException ("Invalid message type: " + obj.getClass().getName());
        } else {

            throw new UnsupportedOperationException ("Invalid message type: " + obj.getClass().getName());
        }
    }

}
