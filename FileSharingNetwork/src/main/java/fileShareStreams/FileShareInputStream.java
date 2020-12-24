package main.java.fileShareStreams;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.ObjectInput;

import main.java.messages.*;

public class FileShareInputStream extends DataInputStream implements ObjectInput {
    private boolean receivedHandshake = false;

    public FileShareInputStream(InputStream in) {
        super(in);
    }

    @Override
    public Object readObject() throws ClassNotFoundException, IOException {
        if (receivedHandshake) {
            final int length = readInt();
            final int payloadLength = length - 1; // subtract 1 for the message type
            Message msg = Message.getInstance(Type.getType(readByte()), payloadLength);
            msg.read(this);
            return msg;
        } else {
            Handshake handshake = new Handshake();
            handshake.read(this);
            receivedHandshake = true;
            return handshake;
        }
    }

}
