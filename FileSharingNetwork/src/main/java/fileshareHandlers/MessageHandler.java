package main.java.fileshareHandlers;


import java.util.BitSet;

import main.java.messages.*;
import main.java.fileshareHandlers.*;
import main.java.log.*;

public class MessageHandler {

    private boolean isChoked;
    private final int senderID;
    private final FileShareLogger logger;

    MessageHandler(int id, FileShareLogger fsLogger) {
    	isChoked = true;
    	senderID = id;
        logger = fsLogger;
    }

    public Message handleHandshake(Handshake handshake) {
        BitSet bitfiled = FileHandler.INSTANCE.getReceivedPieces();
        if (!bitfiled.isEmpty()) return (new Bitfield(bitfiled));

        return null;
    }

    public synchronized Message handle(Message msg) {
        switch(msg.getType()) {
        	case Choke:
        		isChoked = true;
        		logger.chokeMessage(senderID);
        		return null;

        	case Unchoke:
        		isChoked = false;
        		logger.unchokeMessage(senderID);
        		if (!isChoked) {
        			BitSet availablePieces = PeerHandler.INSTANCE.getReceivedPieces(senderID);
        			int nextPieceIndex = FileHandler.INSTANCE.selectNextPiece(availablePieces);
	                if (nextPieceIndex >= 0) {
	                     return new Request (nextPieceIndex);
	                }
                 }
                 return null;

            case Interested:
                PeerHandler.INSTANCE.addInterestedPeer(senderID);
                logger.interestedMessage(senderID);
                return null;

            case NotInterested:
                PeerHandler.INSTANCE.removeInterestedPeer(senderID);
                logger.notInterestedMessage(senderID);
                return null;

            case Have:
                Have have = new Have(msg.payload);
                final int pieceIndex = have.getPieceIndex();
                logger.haveMessage(senderID, pieceIndex);

                PeerHandler.INSTANCE.receiveHAVE(senderID, pieceIndex);

                if (FileHandler.INSTANCE.getReceivedPieces().get(pieceIndex)){
                    return null;
                } else {
                    return new Message(Type.Interested);
                }

            case Bitfield:
                Bitfield bmsg = (Bitfield) msg;
                BitSet bitfield = bmsg.getBitfield();
                PeerHandler.INSTANCE.receiveBITFIELD(senderID, bitfield);
                bitfield.andNot(FileHandler.INSTANCE.getReceivedPieces());

                if (bitfield.isEmpty()) {
                    return new Message(Type.NotInterested);
                } else {
                    return new Message(Type.Interested);
                }

            case Request:
                Request rmsg = (Request) msg;
                if (PeerHandler.INSTANCE.isValidPeer(senderID)) {
                    byte[] piece = FileHandler.INSTANCE.getPiece(rmsg.getPieceIndex());
                    if (piece != null) return new Piece(rmsg.getPieceIndex(), piece);
                }
                return null;

            case Piece:
                Piece piece = (Piece) msg;
                FileHandler.INSTANCE.addPiece(piece.getPieceIndex(), piece.getContent());
                PeerHandler.INSTANCE.receivePIECE(senderID, piece.getContent().length);
                logger.pieceDownloadedMessage(senderID, piece.getPieceIndex());
                if (!isChoked) {
        			BitSet availablePieces = PeerHandler.INSTANCE.getReceivedPieces(senderID);
        			int nextPieceIndex = FileHandler.INSTANCE.selectNextPiece(availablePieces);
	                if (nextPieceIndex >= 0) {
	                     return new Request (nextPieceIndex);
	                 }
                 }
                 return null;
        	}
        return null;
    }
}
