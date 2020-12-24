package main.java.fileshareHandlers;

import java.util.Collection;

public interface PeerListener {
    public void chokePeers (Collection<Integer> chokedPeers);
    public void unchokePeers (Collection<Integer> unchokedPeers);
    
    public void neighborsCompletedDownload();

}
