package main.java.configs;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public enum Common {
	PROPERTIES("src/main/resources/Common.cfg");
	
    private int numberOfPreferredNeighbors;
	private int unchokingInterval;
	private int optimisticUnchokingInterval;
	private String fileName;
	private int fileSize;
	private int pieceSize;

	private Common (String src) {
		Properties prop = new Properties () {
          @Override
          public synchronized void load(Reader reader) throws IOException {
              BufferedReader in = new BufferedReader(reader);
              String line;
              while ((line = in.readLine()) != null) {
                  String[] tokens = line.split("\\s+");
                  setProperty(tokens[0].trim(), tokens[1].trim());
              }
          }
      };
      try {
          prop.load(new FileReader(src));
          this.numberOfPreferredNeighbors = Integer.parseInt(prop.getProperty("NumberOfPreferredNeighbors"));
          this.unchokingInterval = Integer.parseInt(prop.getProperty("UnchokingInterval"));
          this.optimisticUnchokingInterval = Integer.parseInt(prop.getProperty("OptimisticUnchokingInterval"));
          this.fileName = prop.getProperty("FileName");
          this.fileSize = Integer.parseInt(prop.getProperty("FileSize"));
          this.pieceSize = Integer.parseInt(prop.getProperty("PieceSize"));          
      } catch (IOException e) {
    	  e.printStackTrace();
      }      
	}
		
	public int getNumberOfPreferredNeighbors() {
		return numberOfPreferredNeighbors;
	}
	
	public int getUnchokingInterval() {
		return unchokingInterval;
	}
	
	public int getOptimisticUnchokingInterval() {
		return optimisticUnchokingInterval;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public int getFileSize() {
		return fileSize;
	}
	
	public int getPieceSize() {
		return pieceSize;
	}
}
