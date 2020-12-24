package main.java.configs;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class RemotePeerInfo {
    public final int peerID;
    public final int peerPort;
    public final String hostName;
    public final boolean hasFile;
    
    public AtomicInteger bytesDownloaded;
    public BitSet receivedPieces;
    private final AtomicBoolean interested;

    public RemotePeerInfo (int peerID) {
        this (peerID, "127.0.0.1", 80, false);
    }

    public RemotePeerInfo(int id, String hostName, int port, boolean hasFile) {
    	
    	this.peerID = id;
    	this.hostName = hostName;
    	this.peerPort = port;
        this.hasFile = hasFile;
        
        this.bytesDownloaded = new AtomicInteger (0);
        this.receivedPieces = new BitSet();
        this.interested = new AtomicBoolean (false);
    }

    public boolean isInterested() {
        return interested.get();
    }

    public void setInterested() {
        interested.set (true);
    }

    public void setNotIterested() {
        interested.set (false);
    }

    @Override
    public boolean equals (Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof RemotePeerInfo) {
            return (((RemotePeerInfo) obj).peerID == (peerID));
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.peerID);
        return hash;
    }

    @Override
    public String toString() {
        return new StringBuilder (peerID)
                .append (" host name: ")
                .append (hostName)
                .append(" port: ")
                .append(peerPort).toString();
    }
    
    public static Collection<Integer> getAllIDs (Collection<RemotePeerInfo> peers) {
        Set<Integer> ids = new HashSet<>();
        for (RemotePeerInfo peer : peers) {
            ids.add(peer.peerID);
        }
        return ids;
    }
    
    public static class SortByDownloadSpeed implements Comparator<RemotePeerInfo> {
    	@Override
    	public int compare(RemotePeerInfo rp1, RemotePeerInfo rp2) {
    		return (int) (rp2.bytesDownloaded.get() - rp1.bytesDownloaded.get());
    	}
    }

}

