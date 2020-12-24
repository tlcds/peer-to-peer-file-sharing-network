package main.java.messages;

public class Request extends Message {
	
    Request(byte[] payload) {
        super(Type.Request, payload);
    }
    
    public Request(int pieceIndex) {
    	super(Type.Request, Utils.intToByteArray(pieceIndex));        
    }
}
