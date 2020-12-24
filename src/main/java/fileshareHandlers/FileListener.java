package main.java.fileshareHandlers;

public interface FileListener {
	public void fileCompleted();
    public void receivePiece (int pieceIndex);
}
