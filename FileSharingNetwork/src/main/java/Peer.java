package main.java;

import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.*;
import java.io.IOException;


import main.java.configs.*;
import main.java.fileshareHandlers.*;
import main.java.messages.*;
import main.java.log.*;


public class Peer implements Runnable, FileListener, PeerListener{

	private final int peerID;
    private final int port;
    private final boolean hasFile;
    private final FileShareLogger fsLogger;

    private final AtomicBoolean collectFullFile = new AtomicBoolean(false);
    private final AtomicBoolean othersHasCompleted = new AtomicBoolean(false);
    private final AtomicBoolean isTerminated = new AtomicBoolean(false);
    private final Collection<ConnectionHandler> connections =
    		Collections.newSetFromMap(new ConcurrentHashMap<ConnectionHandler, Boolean>());
    
    private ServerSocket serverSocket;

    
	public Peer(int id, int port, boolean hasFile) {
		this.peerID = id;
		this.port = port;
		this.hasFile = hasFile;
		collectFullFile.set(hasFile);

		FileHandler.INSTANCE.setID(id);

		ArrayList<RemotePeerInfo> remotePeers = new ArrayList<>(PeerInfo.INSTANCE.getAllPeerInfo());
		for (RemotePeerInfo remotePeer: remotePeers) {
			if (remotePeer.peerID == peerID) {
				remotePeers.remove(remotePeer);
				break;
			}
		}
		
		PeerHandler.INSTANCE.init(peerID, (FileHandler.INSTANCE.getNumOfPieces()), remotePeers);
		fsLogger = new FileShareLogger(peerID);

	}

	void init() {
		FileHandler.INSTANCE.addListener(this);
		PeerHandler.INSTANCE.addListener(this);

		if (hasFile) {
			FileHandler.INSTANCE.getFullFile();
			FileHandler.INSTANCE.splitFile();
			FileHandler.INSTANCE.setAllPiecesReceived();
		}

		Thread phThread = new Thread(PeerHandler.INSTANCE);
		phThread.setName(PeerHandler.INSTANCE.getClass().getName());
		phThread.start();
    }

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			while (!isTerminated.get()) {
				try {
					buildConnection(new ConnectionHandler(serverSocket.accept(), peerID));
				} catch (Exception e) {
					FSLogger.getLogger().warning(e);
				}
			}
		} catch (IOException e) {
			FSLogger.getLogger().warning(e);
		} finally {
			FSLogger.getLogger().warning(Thread.currentThread().getName() + "is terminating ...");
		}
	}

	void connectToPeers(Collection<RemotePeerInfo> peers) {
        Iterator<RemotePeerInfo> iter = peers.iterator();
        while (iter.hasNext()) {
            do {
                Socket socket = null;
                RemotePeerInfo peerToConnect = iter.next();
                try {
                    socket = new Socket(peerToConnect.hostName, peerToConnect.peerPort);

                    if (buildConnection(new ConnectionHandler(socket, peerID, peerToConnect.peerID, true))){
                        iter.remove();
                    }
                } catch (IOException e) {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ioe) {}
                    }
                    FSLogger.getLogger().warning(e);
                }
            } while (iter.hasNext());

            iter = peers.iterator();
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {}
        }
    }

	private synchronized boolean buildConnection(ConnectionHandler connection) {
        if (connections.contains(connection)) return false;

		connections.add(connection);
		new Thread(connection).start();
		try {
			wait(5);
		} catch (InterruptedException e) {
			FSLogger.getLogger().warning(e);
		}
		return true;
	}

	@Override // FileListener
	public synchronized void fileCompleted() {
		fsLogger.fileDownloadedMessage();
		collectFullFile.set(true);
		if (othersHasCompleted.get()) {
			isTerminated.set(true);
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
	}

	@Override // FileListener
	public synchronized void receivePiece(int pieceIndex) {
		for (ConnectionHandler connection: connections) {
			Message msg = new Message(Type.Have, (Utils.intToByteArray(pieceIndex)));
			connection.send(msg);
			if (!PeerHandler.INSTANCE.isInteresting(connection.getRemotePeerID(), FileHandler.INSTANCE.getReceivedPieces())) {
				connection.send(new Message(Type.NotInterested));
			}
		}
	}

	@Override // PeerListener
	public synchronized void chokePeers(Collection<Integer> unchokedPeers) {
		for (ConnectionHandler connection: connections) {
			if (unchokedPeers.contains(connection.getRemotePeerID())) {
				connection.send(new Message(Type.Choke));
			}
		}
	}

	@Override // PeerListener
	public synchronized void unchokePeers(Collection<Integer> chokedPeers) {
		for (ConnectionHandler connection: connections) {
			if (chokedPeers.contains(connection.getRemotePeerID())) {
				connection.send(new Message(Type.Unchoke));
			}
		}
	}

	@Override // PeerListener
	public void neighborsCompletedDownload() {
		othersHasCompleted.set(true);
		if (collectFullFile.get()) {
			isTerminated.set(true);
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				System.exit(0);
			}
		}
	}

}
