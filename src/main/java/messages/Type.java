package main.java.messages;

public enum Type {
	Choke ((byte) 0),
    Unchoke ((byte) 1),
    Interested ((byte) 2),
    NotInterested ((byte) 3),
    Have ((byte) 4),
    Bitfield ((byte) 5),
    Request ((byte) 6),
    Piece ((byte) 7);
	
	private byte value;
	
	Type(byte val) { value = val;}
	
	public byte getValue() { return value;}
	
	public static Type getType(int val) {
		switch(val) {
			case 0: return Choke;
			case 1: return Unchoke;
			case 2: return Interested;
			case 3: return NotInterested;
			case 4: return Have;
			case 5: return Bitfield;
			case 6: return Request;
			case 7: return Piece;
			default: throw new IllegalArgumentException("Type value ranges from 0 to 7!");
		}
	}
	
	public static Type getType (byte val) {
		switch(val) {
			case 0: return Choke;
			case 1: return Unchoke;
			case 2: return Interested;
			case 3: return NotInterested;
			case 4: return Have;
			case 5: return Bitfield;
			case 6: return Request;
			case 7: return Piece;
			default: throw new IllegalArgumentException("Type value ranges from 0 to 7!");
		}
	}
	
}
