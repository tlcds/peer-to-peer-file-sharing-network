package main.java.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Message {
	public int length;
	public Type type;
	public byte[] payload;
	
	
    public Message (Type type) {
        this.type = type;
    }

    public Message (Type type, byte[] payload) {
        this.length = (payload == null ? 0 : payload.length) + 1;
        this.type = type;
        this.payload = payload;        
    }

    public Type getType() {return type;}
    
    public int getPieceIndex() {
    	return ByteBuffer.wrap(Arrays.copyOfRange(payload,0,4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }

    public void read (DataInputStream in) throws IOException {
    	if (payload == null || payload.length == 0) return;
    	in.readFully(payload, 0, payload.length);
    }

    public void write (DataOutputStream out) throws IOException {
        out.writeInt(length);
        out.writeByte(type.getValue());
        if (payload != null && payload.length > 0) out.write(payload, 0, payload.length);
    }

    public static Message getInstance (Type type, int length) throws ClassNotFoundException, IOException {
        switch (type) {
            case Choke: 		return new Message(Type.Choke);
            case Unchoke: 		return new Message(Type.Unchoke);
            case Interested:	return new Message(Type.Interested);
            case NotInterested:	return new Message(Type.NotInterested);
            case Have:			return new Message(Type.Have, new byte[length]);
            case Bitfield:		return new Bitfield(new byte[length]);
            case Request:		return new Request(new byte[length]);
            case Piece:			return new Piece(new byte[length]);
            default:
                throw new ClassNotFoundException ("Invalid message type: " + type.toString());
        }
    }
}
