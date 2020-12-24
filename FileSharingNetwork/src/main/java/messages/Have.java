package main.java.messages;

public class Have extends Message{
	public int pieceIndex;
	public Have(byte[] payload) {
		super(Type.Have, payload);
	}
	
	Have(int pieceIndex) {
		this(Utils.intToByteArray(pieceIndex));	
	}

}
