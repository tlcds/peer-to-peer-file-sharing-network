package main.java;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.LinkedList;

import main.java.configs.*;
import main.java.log.*;


public class peerProcess {

    public static void main (String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        if (args.length != 1) FSLogger.getLogger().severe("Please provide single argument as peerID");

        final int peerID = Integer.parseInt(args[0]);
        int port = 5000 + peerID;
        String hostName = "127.0.0.1";
        boolean hasFile = false;
        FSLogger.configure(peerID);

		Reader commonReader = null;
        Reader peerInfoReader = null;



        LinkedList<RemotePeerInfo> peersToConnectTo = new LinkedList<>();
        try {
            for (RemotePeerInfo peer : PeerInfo.INSTANCE.getAllPeerInfo()) {
                if (peerID == peer.peerID) {
                	hostName = peer.hostName;
                    port = peer.peerPort;
                    hasFile = peer.hasFile;
                    break;
                } else {
                    peersToConnectTo.add (peer);
                }
            }
        } catch (Exception ex) {
        	FSLogger.getLogger().severe (ex);
            return;
        } finally {
            try {
            	commonReader.close();
            	peerInfoReader.close();
            } catch (Exception e) {}
        }

        Peer localPeer = new Peer(peerID, port, hasFile);
        localPeer.init();
        Thread myPeerThread = new Thread (localPeer);
        myPeerThread.setName ("peer_" + peerID);
        myPeerThread.start();
        localPeer.connectToPeers (peersToConnectTo);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
