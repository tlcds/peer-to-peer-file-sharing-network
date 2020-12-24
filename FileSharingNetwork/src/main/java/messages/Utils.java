package main.java.messages;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class Utils {

	private Utils() {}
	
	public static byte[] intToByteArray (int num) {
        return ByteBuffer.allocate(4).putInt(num).array();
    }
	
	public static int byteArrayToInt(byte[] arr) {
        return ByteBuffer.wrap(Arrays.copyOfRange(arr, 0, 4)).order(ByteOrder.BIG_ENDIAN).getInt();
    }
}
