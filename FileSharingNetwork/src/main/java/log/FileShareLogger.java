package main.java.log;

public class FileShareLogger {
    private final FSLogger fsLogger;
    private final String header;

    public FileShareLogger (int peerID) {
        this (peerID, FSLogger.getLogger());
    }

    public FileShareLogger (int peerID, FSLogger logger) {
        fsLogger = logger;
    	header = String.format(": Peer %d", peerID);
    }

    public void TCPConnection (int peerID, boolean isConnectingTo) {
        String message = header + (isConnectingTo ? " makes a connection to Peer %d." : " is connected from Peer %d.");
        fsLogger.info (String.format (message, peerID));
    }

    public void changeOfPrefereedNeighbors (String preferredNeighbors) {
        String message = header + " has preferred neighbors %s";
        fsLogger.info (String.format (message, preferredNeighbors));
    }

    public void changeOfOptimisticallyUnchokedNeighbors (String preferredNeighbors) {
        String message = header + " has the optimistically unchoked neighbor %s";
        fsLogger.info (String.format (message, preferredNeighbors));
    }

    public void chokeMessage (int peerId) {
        String message = header + " is choked by %d.";
        fsLogger.info (String.format (message, peerId));
    }

    public void unchokeMessage (int peerId) {
        String message = header + " is unchoked by %d.";
        fsLogger.info (String.format (message, peerId));
    }

    public void haveMessage (int id, int pieceIndex) {
        String message = header + " received the 'have' message from %d for the piece %d.";
        fsLogger.info (String.format (message, id, pieceIndex));
    }

    public void interestedMessage (int peerId) {
        String message = header + " received the 'interested' message from %d.";
        fsLogger.info (String.format (message, peerId));
    }

    public void notInterestedMessage (int peerId) {
        String message = header + " received the 'not interested' message from %d.";
        fsLogger.info (String.format (message, peerId));
    }

    public void pieceDownloadedMessage (int peerId, int pieceIdIndex) {
        String message = header + " has downloaded the piece %d from peer %d.";
        fsLogger.info (String.format (message, pieceIdIndex, peerId));
    }

    public void fileDownloadedMessage () {
        String message = header + " has downloaded the complete file.";
        fsLogger.info (String.format (message));
    }


}
