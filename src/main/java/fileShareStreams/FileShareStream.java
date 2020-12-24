package main.java.fileShareStreams;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public interface FileShareStream {
	public void read(DataInputStream in)throws IOException;
    public void write(DataOutputStream out)throws IOException;
}
