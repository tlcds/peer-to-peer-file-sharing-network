package main.java.fileUtils;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

import main.java.fileshareHandlers.FileHandler;
import main.java.log.*;

public class FileUtil {
	public final File file;
    public final File piecesDir;

    public FileUtil(int id, String fileName){
        piecesDir = new File("./peer_" + id + "/pieces");
        piecesDir.mkdirs();
        file = new File(piecesDir.getParent() + "/" + fileName);
    }

    public byte[][] getAllPiecesAsByteArrays(){
        File[] files = piecesDir.listFiles (new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) { return true; }
        });
        byte[][] byteArr = new byte[files.length][getPieceByIndex(1).length];
        for (File file : files) {
        	byteArr[Integer.parseInt(file.getName())] = getByteArrayFromFile(file);
        }
        return byteArr;
    }

    public byte[] getPieceByIndex(int pieceIndex) {
        File file = new File(piecesDir.getAbsolutePath() + "/" + pieceIndex);
        return getByteArrayFromFile(file);
    }

    public void writeByteArrayToPieceFile(byte[] piece, int pieceIndex){
        FileOutputStream fileoutputstream;
        File pieceFile = new File(piecesDir.getAbsolutePath() + "/" + pieceIndex);
        try {
            fileoutputstream = new FileOutputStream(pieceFile);
            fileoutputstream.write(piece);
            fileoutputstream.flush();
            fileoutputstream.close();
        } catch (FileNotFoundException e) {
            FSLogger.getLogger().warning(e);
        } catch (IOException e) {
            FSLogger.getLogger().warning(e);
        }
    }
    
    public void copyFullFile() {
    	String fname = FileHandler.INSTANCE.getFileName();
    	Path src = Paths.get("src/main/resources/" + fname);
    	Path target = Paths.get(piecesDir.getParent() + '/'+ fname);
    	try {
    		Files.copy(src, target);
    	} catch (Exception e) {}
    	
    }
        
    public byte[] getByteArrayFromFile(File file){
        FileInputStream fis = null;
        try {
        	fis = new FileInputStream(file);
            byte[] fileBytes = new byte[(int) file.length()];
            long length = file.length();
            int bytesRead = fis.read(fileBytes, 0, (int) length);
            fis.close();
            assert (bytesRead == fileBytes.length);
            assert (bytesRead == (int) length);
            return fileBytes;
        } catch (FileNotFoundException e) {
            FSLogger.getLogger().warning(e);
        } catch (IOException e) {
            FSLogger.getLogger().warning(e);
        }
        finally {
            if (fis != null) {
                try {
                	fis.close();
                }
                catch (IOException ex) {}
            }
        }
        return null;
    }

    public void splitFile(int pieceSize){       
    	FileInputStream fis;//inputStream;
        String newFileName;
        FileOutputStream fos;//filePart;
        int fileSize = (int) file.length();
        int pieceIndex = 0, read = 0, readLength = pieceSize; 
        byte[] piece; 
        try {
            fis = new FileInputStream(file);            
            while (fileSize > 0) {
                if (fileSize <= 5) readLength = fileSize;
                    			 
                piece = new byte[readLength];
                read = fis.read(piece, 0, readLength);
                if(read == -1) break;
                fileSize -= read;                
                newFileName = file.getParent() + "/pieces/" + Integer.toString(pieceIndex++);
                fos = new FileOutputStream(new File(newFileName));
                fos.write(piece);
                fos.flush();
                fos.close();
                piece = null;
                fos = null;
            }
            fis.close();
        } catch (IOException e) {
            FSLogger.getLogger().warning(e);
        }
    }

    public void mergeFile(int PartsNumber) {
        File fullFile = file;
        FileOutputStream fos;
        FileInputStream fis;
        byte[] fileBytes;
        int bytesRead = 0;
        List<File> content = new ArrayList<>();
        for (int i = 0; i < PartsNumber; i++) {
            content.add(new File(piecesDir.getPath() + "/" + i));
        }
        try {
        	fos = new FileOutputStream(fullFile);
            for (File file : content) {
                fis = new FileInputStream(file);
                fileBytes = new byte[(int) file.length()];
                long length = file.length();
                bytesRead = fis.read(fileBytes, 0, (int) length);
                assert (bytesRead == fileBytes.length);
                assert (bytesRead == (int) length);
                fos.write(fileBytes);
                fos.flush();
                fileBytes = null;
                fis.close();
                fis = null;
            }
            fos.close();
            fos = null;
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
