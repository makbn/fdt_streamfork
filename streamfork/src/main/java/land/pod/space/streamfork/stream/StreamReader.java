package land.pod.space.streamfork.stream;


import land.pod.space.streamfork.exception.ProtocolException;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by white on 2016-08-06.
 */
public class StreamReader {

    public static byte[] read(InputStream inputStream, int size) {
        byte[] bytes = new byte[size];
        int readLength = 0;
        int remainingLength = size;
        int lastRead = 0;

        while (readLength < size) {
            int countToRead = Math.min(remainingLength, 2048);
            byte[] byteBuffer = new byte[countToRead];
            try {
                lastRead = inputStream.read(byteBuffer, 0, countToRead);
            } catch (Exception e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            if (lastRead <= 0) {
                Thread.currentThread().interrupt();
                throw ProtocolException.getInstance("read size is " + lastRead);
            }
            byteAppendHelper(bytes, byteBuffer, lastRead, readLength);
            readLength += lastRead;
            remainingLength -= lastRead;
        }
        return bytes;
    }


    private static byte[] byteAppendHelper(byte[] dest, byte[] src, int lenght, int pos) {
        System.arraycopy(src, 0, dest, pos, lenght);
        return dest;
    }
}
