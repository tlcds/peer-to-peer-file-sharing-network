package main.java.fileshareHandlers;

import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Timer;
import java.util.TimerTask;
import java.io.IOException;


import main.java.fileShareStreams.*;
import main.java.messages.*;
import main.java.log.*;

public class ConnectionHandler implements Runnable {

    private static final int PEER_ID_UNSET = -1;

    private final Socket socket;
    private final int myPeerID;
    private final int expectedRemotePeerID;
    private final AtomicInteger remotePeerID;
    private final boolean isConnecting;

    private final FileShareOutputStream out;

    private final BlockingQueue<Message> sendingQueue = new LinkedBlockingQueue<>();

    public ConnectionHandler(Socket socket, int myID) throws IOException {
        this(socket, myID, -1, false);
    }

    public ConnectionHandler(Socket socket, int myID, int expectedRemotePeerId, boolean isConnecting) throws IOException {
        this.socket = socket;
        this.myPeerID = myID;
        this.expectedRemotePeerID = expectedRemotePeerId;
        this.remotePeerID = new AtomicInteger(PEER_ID_UNSET);
        this.isConnecting = isConnecting;
        this.out = new FileShareOutputStream(socket.getOutputStream());
    }

    public int getRemotePeerID() { return remotePeerID.get(); }

    @Override
    public void run() {
        new Thread() {

            private boolean isRemotePeerChoked = true;

            @Override
            public void run() {
                Thread.currentThread().setName("[CHandler Thread] Peer " + remotePeerID);
                while (true) {
                    try {
                        final Message message = sendingQueue.take();

                        if (message == null) continue;

                        if (remotePeerID.get() != PEER_ID_UNSET) {
                            switch (message.getType()) {
                                case Choke:
                                    if (!isRemotePeerChoked) {
                                    	isRemotePeerChoked = true;
                                    	out.writeObject(message);
                                    }
                                    break;
                                case Unchoke:
                                    if (isRemotePeerChoked) {
                                    	isRemotePeerChoked = false;
                                    	out.writeObject(message);
                                    }
                                    break;
                                default: handleRequest(message);
                            }
                        }
                    } catch (Exception e) {
                        FSLogger.getLogger().warning(e);
                    }
                }
            }
        }.start();

        /* Handshake */
        try {
            final FileShareInputStream in = new FileShareInputStream(socket.getInputStream());
            out.writeObject(new Handshake(myPeerID));

            Handshake handshake = (Handshake) in.readObject();
            remotePeerID.set(handshake.getPeerID());

            Thread.currentThread().setName(getClass().getName() + "-" + remotePeerID.get());

            final FileShareLogger fsLogger = new FileShareLogger(myPeerID);
            final MessageHandler messageHandler = new MessageHandler(remotePeerID.get(), fsLogger);

            if (isConnecting && (remotePeerID.get() != expectedRemotePeerID)) {
            	throw new Exception("Remote peer id " + ((int)remotePeerID.get()) + " does not match with the expected id: " + expectedRemotePeerID);
            }


            fsLogger.TCPConnection(remotePeerID.get(), isConnecting);

            Message bf;
            if((bf = messageHandler.handleHandshake(handshake)) != null ) out.writeObject(bf);

            while (true) {
                try {
                    handleRequest(messageHandler.handle((Message)in.readObject()));
                } catch (Exception e) {
                    FSLogger.getLogger().warning(e);
                    break;
                }
            }
        } catch (Exception e) {
        	FSLogger.getLogger().warning(e);
        } finally {
            try {
                socket.close();
            } catch (Exception e) {}
        }
        FSLogger.getLogger().warning(
        		Thread.currentThread().getName() + " terminating, messages will no longer be accepted.");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ConnectionHandler) {
            return ((ConnectionHandler) obj).remotePeerID == remotePeerID;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 8;
        hash = 50 * hash + myPeerID;
        return hash;
    }

    public void send(final Message message) {
        sendingQueue.add(message);
    }

    private synchronized void handleRequest(Message message) throws IOException {
    /* Keep sending request*/
        if (message != null) {
            out.writeObject(message);
            if(message.getType() == Type.Request) {
            	Timer timer = new Timer();
            	timer.schedule(
            			new RequestTimerTask((Request)message),
            			FileHandler.INSTANCE.getUnchokingInterval() * 2
                );
            }
        }
    }

    class RequestTimerTask extends TimerTask{
    	Request request;
    	RequestTimerTask (Request req) { request = req; }
    	@Override
        public void run() {
    		if (!FileHandler.INSTANCE.hasPiece(request.getPieceIndex())) {
    			 try {
                     out.writeObject(request);
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
    		}
        }
    }
}

