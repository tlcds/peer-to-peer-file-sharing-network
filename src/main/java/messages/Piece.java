package main.java.messages;

import java.util.Arrays;

public class Piece extends Message{
	Piece (byte[] payload) {
        super(Type.Piece, payload);
    }

    public Piece (int pieceIndex, byte[] payload) {
        super(Type.Piece, addPiece(pieceIndex, payload));
    }

    public byte[] getContent() {
        if (payload == null || payload.length <= 4) {
            return null;
        } else {
        	return Arrays.copyOfRange(payload, 4, payload.length);
        }        
    }

    private static byte[] addPiece(int pieceIndex, byte[] payload) {
        byte[] result = payload == null ? new byte[4] : new byte[4 + payload.length];        
        System.arraycopy(Utils.intToByteArray(pieceIndex), 0, result, 0, 4);
        System.arraycopy(payload, 0, result, 4, payload.length);
        return result;
    }

}
