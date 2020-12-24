package main.java.messages;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.util.Arrays;

import main.java.fileShareStreams.*;

public class Handshake implements FileShareStream{
    private String HANDSHAKE_HEADER = "P2PFILESHARINGPROJ";
    private byte[] zeros = new byte[10];
    private byte[] peerID = new byte[4];

    public Handshake() {}

    public Handshake (int id) {
        this (Utils.intToByteArray(id));
    }

    private Handshake (byte[] pID) {
        if (pID.length > 4) {
            throw new ArrayIndexOutOfBoundsException(
            		String.format("Invalid peer ID length! (ID:%d, length: %d)", Arrays.toString (pID), pID.length));
        }
        System.arraycopy(pID, 0, peerID, 0, 4);
    }

    @Override
	public void write(DataOutputStream out) throws IOException {
        byte[] header = HANDSHAKE_HEADER.getBytes(Charset.forName("UTF-8"));
        
        if (header.length != HANDSHAKE_HEADER.length()) throw new IOException();
        
        out.write(header, 0, header.length);
        out.write(zeros, 0, zeros.length);
        out.write(peerID, 0, peerID.length);
    }

    @Override
    public void read (DataInputStream in) throws IOException {
        byte[] header = new byte[HANDSHAKE_HEADER.length()];

        // Check header length
        if (in.read(header, 0, HANDSHAKE_HEADER.length()) < HANDSHAKE_HEADER.length()) {
        	throw new ProtocolException (
            		String.format("[Handshake.read] Handshake message header should be %s, but received header is %s",
            				HANDSHAKE_HEADER, Arrays.toString(header)));
        }
        // Check header 
        if (!HANDSHAKE_HEADER.equals (new String(header, Charset.forName("UTF-8")))) {
            throw new ProtocolException (
            		String.format("The handshake header should be %s, but received header is %s",
            				HANDSHAKE_HEADER, Arrays.toString(header)));
        }
        // Check zeros
        if (in.read(zeros, 0, zeros.length) <  zeros.length) {
            throw new ProtocolException (
            		String.format("A handshake message should contain 10-byte zeros, but only received %d bytes",
            				zeros.length));
        }
        // Check peerID
        if (in.read(peerID, 0, peerID.length) <  peerID.length) {
            throw new ProtocolException (
            		String.format("peerID should be 4-byte, but only received %d bytes",
            				peerID.length));
        }
    }

    public int getPeerID() {
        return Utils.byteArrayToInt(peerID);
    }

}
