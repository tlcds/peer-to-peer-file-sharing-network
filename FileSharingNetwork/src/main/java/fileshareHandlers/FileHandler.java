package main.java.fileshareHandlers;

import java.util.BitSet;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Properties;


import main.java.configs.*;
import main.java.fileUtils.*;
import main.java.log.*;

public enum FileHandler{
	INSTANCE;

	private int fileSize;
	private int pieceSize;
	private int numOfPieces;
	private int unchokingInterval;
	private String fileName;
	private BitSet receivedPieces;
    private RequestedPieces beingRequestedPieces;
    private final Collection<FileListener> listeners = new LinkedList<>();

    private FileUtil fileUtil;
    
    private FileHandler() {
    	Common common = Common.PROPERTIES;
    	fileSize = common.getFileSize();

    	pieceSize = common.getPieceSize();

    	numOfPieces = (int) Math.ceil (fileSize*1.0 / pieceSize);

    	fileName = common.getFileName();

    	unchokingInterval = common.getUnchokingInterval() * 1000;

    	receivedPieces = new BitSet (numOfPieces);

    	beingRequestedPieces = new RequestedPieces (numOfPieces, unchokingInterval);
    }
    
    public FileHandler getInstance() {
    	return INSTANCE;
    }
    
    public void setID(int id) {
    	fileUtil = new FileUtil(id, fileName);
    }
//    public void init(int pid, Properties common) {
//    	peerID = pid;
//
//    	fileSize = Integer.parseInt(CommonProperties.FileSize.toString());
//
//    	pieceSize = Integer.parseInt(CommonProperties.PieceSize.toString());
//
//    	numOfPieces = (int) Math.ceil (fileSize*1.0 / pieceSize);
//
//    	fileName = CommonProperties.FileName.toString();
//
//    	unchokingInterval = 1000 * Integer.parseInt(common.getProperty(CommonProperties.UnchokingInterval.toString()));
//
//    	receivedPieces = new BitSet (numOfPieces);
//
//    	beingRequestedPieces = new RequestedPieces (numOfPieces, unchokingInterval);
//
//    	fileUtil = new FileUtil(peerID, fileName);
//    }

    /* Getters */
    public int getFileSize() { return fileSize; }

    public int getPieceSize() { return pieceSize; }

    public int getNumOfPieces() { return numOfPieces; }

    public String getFileName() { return fileName; }

    public long getUnchokingInterval() { return unchokingInterval; }

    public synchronized BitSet getReceivedPieces() { return (BitSet) receivedPieces.clone(); }

    public synchronized RequestedPieces getBeingRequestedPieces() { return beingRequestedPieces; }


    public void addListener (FileListener listener) {
        listeners.add (listener);
    }


    public synchronized void addPiece (int pieceIndex, byte[] piece) {

        final boolean isNewPiece = !receivedPieces.get(pieceIndex);
        receivedPieces.set (pieceIndex);
        
        if (isNewPiece) {
            fileUtil.writeByteArrayToPieceFile(piece, pieceIndex);
            for (FileListener listener : listeners) {
                listener.receivePiece(pieceIndex);
            }
        }

        if (isFileCompleted()) {
            fileUtil.mergeFile(receivedPieces.cardinality());
            for (FileListener listener : listeners) {
                listener.fileCompleted();
            }

        }
    }

    public synchronized byte[] getPiece (int pieceIndex) {
    	byte[] piece = fileUtil.getPieceByIndex(pieceIndex);
    	return piece;
    }

    public synchronized boolean hasPiece(int pieceIndex) {
        return receivedPieces.get(pieceIndex);
    }

    public synchronized int selectNextPiece(BitSet availablePieces) {
    	availablePieces.andNot(getReceivedPieces());
        return beingRequestedPieces.getPieceToRequest(availablePieces);
    }

    public synchronized void setAllPiecesReceived(){
        for (int i = 0; i < numOfPieces; i++) {
            receivedPieces.set(i, true);
        }
    }

    public synchronized int getNumOfReceivedPieces() {
        return receivedPieces.cardinality();
    }


    public byte[][] getAllPieces(){
        return fileUtil.getAllPiecesAsByteArrays();
    }

    public synchronized void getFullFile() {
    	fileUtil.copyFullFile();
    }

    public void splitFile(){
        fileUtil.splitFile(pieceSize);
    }


    private synchronized boolean isFileCompleted() {
        for (int i = 0; i < numOfPieces; i++) {
            if (!receivedPieces.get(i)) {
                //FSLogger.getLogger().debug("[FHandler] Missing piece " + i);
                return false;
            }
        }
        return true;
    }
}
