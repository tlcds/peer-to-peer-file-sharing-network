package main.java.fileshareHandlers;
import java.util.BitSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import main.java.log.*;


public class RequestedPieces {
    private final BitSet requestedPieces;
    private final long timeout;

    RequestedPieces (int pieces, long unchokingInterval) {
        requestedPieces = new BitSet (pieces);
        timeout = unchokingInterval * 2;
    }


    synchronized int getPieceToRequest(BitSet availablePieces) {
        // Filter out the pieces being requested
    	availablePieces.andNot(requestedPieces);

        if (!availablePieces.isEmpty()) {
            final int pieceIndex = getRandomIndex(availablePieces);

            requestedPieces.set(pieceIndex);

            TimerTask task = new TimerTask() {
            	public void run() {
	                    synchronized (requestedPieces) {
	                        requestedPieces.clear(pieceIndex);
	                        }
                }
            };
            Timer timer = new Timer();
            timer.schedule(task, timeout);

            return pieceIndex;
        }
        return -1;
    }

    private int getRandomIndex (BitSet bitset) {
        if (bitset.isEmpty()) {
            throw new RuntimeException ("Cannot get a random id from an empty bitset!");
        }
        // Generate list of set elements in the format that follows: { 2, 4, 5, ...}
        String set = bitset.toString();
        // Separate the elements, and pick one randomly
        String[] ids = set.substring(1, set.length()-1).split(",");

        return arraySampler(ids);
    }

    private int arraySampler (String[] arr) {
        int rand = new Random().nextInt(arr.length);
        return Integer.parseInt(arr[rand].trim());
    }


}
