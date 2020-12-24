package main.java.fileshareHandlers;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import main.java.configs.*;
import main.java.log.*;


public enum PeerHandler implements Runnable {

	INSTANCE;

    private int numOfPreferred;
	private int unchokingInterval;
	private int numOfPieces;
    private OptimisticUnchoker optUnchoker;
    private FileShareLogger logger;
    private final List<RemotePeerInfo> allPeers = new ArrayList<>();
    private final Collection<RemotePeerInfo> preferredPeers = new HashSet<>();
    private final Collection<PeerListener> listeners = new LinkedList<>();
    private final AtomicBoolean randomSelection = new AtomicBoolean(false);

    public void init (int id, int nPieces, Collection<RemotePeerInfo> peers) {
    	Common common = Common.PROPERTIES;
    	numOfPreferred = common.getNumberOfPreferredNeighbors();
        unchokingInterval = common.getUnchokingInterval() * 1000;
        numOfPieces = nPieces;

        allPeers.addAll(peers);
        optUnchoker = new OptimisticUnchoker();
        logger = new FileShareLogger(id);
    }

    @Override
    public void run() {

        optUnchoker.start();

        while(true) {
            try {
                Thread.sleep(unchokingInterval);
            } catch (InterruptedException e) {}

            List<RemotePeerInfo> interestedPeers = new ArrayList<>();
            for(RemotePeerInfo peer : allPeers){
                if(peer.isInterested()) interestedPeers.add(peer);
            }

            if(randomSelection.get()) {
                Collections.shuffle(interestedPeers);
            } else {
                Collections.sort(interestedPeers, new RemotePeerInfo.SortByDownloadSpeed());
            }

            Collection<Integer> peersToChoke = new HashSet<>();
            Collection<Integer> peersToUnchoke = new HashSet<>();
            Collection<RemotePeerInfo> optUnchokeCandidates  = null;
            Map<Integer, Long> peersDownloadedBytes = new HashMap<>();

            synchronized(this) {

                for(RemotePeerInfo peer : allPeers) {
                	peersDownloadedBytes.put(peer.peerID, peer.bytesDownloaded.longValue());
                    peer.bytesDownloaded.set(0);
                }

                preferredPeers.clear();
                preferredPeers.addAll(interestedPeers.subList(0, Math.min(numOfPreferred, interestedPeers.size())));
                if(preferredPeers.size() > 0) logger.changeOfPrefereedNeighbors(
                		FSLogger.peersToString (preferredPeers));



                Collection<RemotePeerInfo> chokedPeers = new LinkedList<>(allPeers);
                chokedPeers.removeAll(preferredPeers);
                peersToChoke.addAll(RemotePeerInfo.getAllIDs(chokedPeers));

                if(interestedPeers.size() <= numOfPreferred){
                    optUnchokeCandidates = new ArrayList<>();
                } else {
                	optUnchokeCandidates = interestedPeers.subList(numOfPreferred, interestedPeers.size());
                }

                peersToUnchoke.addAll(RemotePeerInfo.getAllIDs(preferredPeers));
            }

            peersToChoke.removeAll(optUnchoker.getOptUnchokedPeers());
            peersToUnchoke.addAll(optUnchoker.getOptUnchokedPeers());

            for(PeerListener listener: listeners) {
                listener.chokePeers(peersToChoke);
                listener.unchokePeers(peersToUnchoke);
            }

            if(optUnchokeCandidates != null) optUnchoker.updateChokeNeighbors(optUnchokeCandidates);

        }
    }

    public synchronized void addListener(PeerListener listener) {
        listeners.add(listener);
    }

    public synchronized void addInterestedPeer(int peerID) {
        RemotePeerInfo peer = getRemotePeer(peerID);
        if(peer != null) peer.setInterested();
    }

    private synchronized RemotePeerInfo getRemotePeer(int peerID) {
        for (RemotePeerInfo peer : allPeers) {
            if (peer.peerID == peerID) return peer;
        }
        FSLogger.getLogger().warning("Cannot find peer " + peerID);
        return null;
    }


    public synchronized void removeInterestedPeer(int peerID) {
        RemotePeerInfo peer = getRemotePeer(peerID);

        if(peer != null) peer.setNotIterested();
    }


    public synchronized boolean isInteresting(int peerID, BitSet myReceivedPieces) {
        RemotePeerInfo remotePeer = getRemotePeer(peerID);

        if(remotePeer == null) return false;

        BitSet availalePieces = (BitSet) remotePeer.receivedPieces.clone();

        availalePieces.andNot(myReceivedPieces);

        return !availalePieces.isEmpty();

    }

    public synchronized boolean isValidPeer(int peerID) {
        RemotePeerInfo peerInfo = new RemotePeerInfo(peerID);

        return(preferredPeers.contains(peerInfo) || optUnchoker.optUnchokedPeers.contains(peerInfo));
    }

    public synchronized void fileCompleted() {
        randomSelection.set(true);
    }

    public synchronized void receiveHAVE(int peerID, int pieceIndex) {
        RemotePeerInfo peer = getRemotePeer(peerID);

        if (peer != null) peer.receivedPieces.set(pieceIndex);

        neighborsCompletedDownload();
    }


    public synchronized void receiveBITFIELD(int peerID, BitSet bitfield) {
        RemotePeerInfo peer = getRemotePeer(peerID);

        if (peer != null) peer.receivedPieces = bitfield;

        neighborsCompletedDownload();
    }


    public synchronized void receivePIECE(int peerID, int contentSize) {
        RemotePeerInfo peer = getRemotePeer(peerID);

        if(peer != null) peer.bytesDownloaded.addAndGet(contentSize);
    }

    public synchronized BitSet getReceivedPieces(int peerID) {
        RemotePeerInfo peer = getRemotePeer(peerID);

        if (peer != null) return (BitSet) peer.receivedPieces.clone();

        return new BitSet();
    }

    private synchronized void neighborsCompletedDownload() {

        for(RemotePeerInfo peer : allPeers) {
            if (peer.receivedPieces.cardinality() < numOfPieces) return;
        }

        for(PeerListener listener : listeners) {
            listener.neighborsCompletedDownload();
        }
    }



    class OptimisticUnchoker extends Thread {

        private final int OPT_UNCHOKING_INTERVAL;
        private final int NUM_OPT_UNCHOKED_NEIGHBORS;

        private final List<RemotePeerInfo> chokedNeighbors = new ArrayList<>();
        private final Collection<RemotePeerInfo> optUnchokedPeers = Collections.newSetFromMap(
        		new ConcurrentHashMap<RemotePeerInfo, Boolean>());

        OptimisticUnchoker() {
            NUM_OPT_UNCHOKED_NEIGHBORS = 1;
            OPT_UNCHOKING_INTERVAL = 
            		Common.PROPERTIES.getOptimisticUnchokingInterval() * 1000;
        }

        synchronized void updateChokeNeighbors(Collection<RemotePeerInfo> neighbors) {
        	chokedNeighbors.clear();
        	chokedNeighbors.addAll(neighbors);
        }

        private Collection<Integer> getOptUnchokedPeers() {
        	return RemotePeerInfo.getAllIDs(optUnchokedPeers);
        }

        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(OPT_UNCHOKING_INTERVAL);
                } catch (InterruptedException e) {}

                synchronized(this) {
                    if (!chokedNeighbors.isEmpty()) {
                        Collections.shuffle(chokedNeighbors);
                        optUnchokedPeers.clear();
                        int size = Math.min(NUM_OPT_UNCHOKED_NEIGHBORS, chokedNeighbors.size());
                        optUnchokedPeers.addAll(chokedNeighbors.subList(0, size));
                    }
                }

                if (chokedNeighbors.size() > 0) {
                    logger.changeOfOptimisticallyUnchokedNeighbors(
                    		FSLogger.peersToString(optUnchokedPeers));
                }

                for (PeerListener listener: listeners) {
                    listener.unchokePeers(RemotePeerInfo.getAllIDs(optUnchokedPeers));
                }
            }
        }
    }


}
