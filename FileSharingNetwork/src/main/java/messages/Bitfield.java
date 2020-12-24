package main.java.messages;

import java.util.BitSet;

public class Bitfield extends Message{
	
    public Bitfield (BitSet bitfield) {
        super (Type.Bitfield, bitfield.toByteArray());
    }
    
	public Bitfield (byte[] bitfield) {
        super (Type.Bitfield, bitfield);
    }
	
	public BitSet getBitfield () {
		return BitSet.valueOf(payload);
	}
}
