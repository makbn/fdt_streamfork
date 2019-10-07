package stream;


import java.io.IOException;
import java.io.InputStream;

/**
 * Created by white on 2016-08-06.
 */
public class StreamReader {


    private byte readByte(InputStream inputStream) throws IOException{
        return read(inputStream,1)[0];
    }


    public byte[] read(InputStream inputStream, int size) throws IOException{
        byte[] bytes=new byte[size];
        int readLength =0;
        int remainingLength=size;
        int lastRead=0;
        
        while (readLength <size) {
            int countToRead = Math.min(remainingLength, 2048);
            byte[] byteBuffer=new byte[countToRead];
            try{
            lastRead = inputStream.read(byteBuffer,0,countToRead);
            }catch(Exception e){
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
            if (lastRead <= 0) {
                Thread.currentThread().interrupt();
                throw new IOException("readed stream size is " + lastRead);
            }
            bytes = byteAppendHelper(bytes, byteBuffer, lastRead, readLength);
            readLength +=lastRead;
            remainingLength -= lastRead;
        }
        return bytes;
    }


    private byte[] byteAppendHelper(byte[] dest,byte[] src,int lenght,int pos){
        System.arraycopy(src,0,dest,pos,lenght);

        return dest;
    }

    private byte[] readHelper(InputStream inputStream,int len){
        byte[] result=new  byte[len];
        for (int i=0;i<len;i++){
            try {
                int t=inputStream.read(result,0,len);
                result[i]= (byte) t;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }
}
