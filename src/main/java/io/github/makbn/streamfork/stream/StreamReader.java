package io.github.makbn.streamfork.stream;


import io.github.makbn.streamfork.exception.ProtocolException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;

/**
 * Created by white on 2016-08-06.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamReader {
    private static final String ERROR_MESSAGE = "read size is %d";
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
               log.error(e.getMessage(), e);
                Thread.currentThread().interrupt();
            }
            if (lastRead <= 0) {
                Thread.currentThread().interrupt();
                throw ProtocolException.getInstance( String.format(ERROR_MESSAGE, lastRead));
            }
            byteAppendHelper(bytes, byteBuffer, lastRead, readLength);
            readLength += lastRead;
            remainingLength -= lastRead;
        }
        return bytes;
    }


    private static void byteAppendHelper(byte[] dest, byte[] src, int length, int pos) {
        System.arraycopy(src, 0, dest, pos, length);
    }
}
