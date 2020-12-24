package main.java.configs;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.Collection;
import java.util.LinkedList;

public enum PeerInfo {
	INSTANCE("src/main/resources/PeerInfo.cfg");
    //public static final String CONFIG_FILE_NAME = "src/main/resources/PeerInfo.cfg";
    private final Collection<RemotePeerInfo> allPeers = new LinkedList<>();

    private PeerInfo (String src) {
    	try(BufferedReader br = new BufferedReader(new FileReader(src))){    		
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                
                if (line.length() <= 0) continue;
                
                String[] info = line.split("\\s+");
                                
                RemotePeerInfo newPeer = new RemotePeerInfo(
        				Integer.valueOf(info[0].trim()),
                		info[1].trim(),
                		Integer.valueOf(info[2].trim()),
                		(info[3].trim().compareTo("1") == 0));
                
                allPeers.add(newPeer);
            }
    	} catch (Exception e) {
        	e.printStackTrace();
        }        
    }
    
    
    public Collection<RemotePeerInfo> getAllPeerInfo() {
    	return new LinkedList<>(allPeers);
    }

}
