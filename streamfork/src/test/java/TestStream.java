import land.pod.space.client.Client;
import land.pod.space.server.Server;
import land.pod.space.stream.StreamBlock;
import land.pod.space.stream.StreamReader;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Mehdi AKbarian-astaghi 10/6/19
 */
public class TestStream {

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        ExecutorService executorService = Executors.newFixedThreadPool(3);
        executorService.execute(() -> server.start(8087));
       /* executorService.execute(() -> server.start(8088));
        executorService.execute(() -> server.start(8089));*/

        StreamReader streamReader = new StreamReader();
        File inputFile = File.createTempFile("temp", "txt");

        FileWriter fileWriter = new FileWriter(inputFile);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.print("Lorem Ipsum is simply dummy text of the printing" +
                " and typesetting industry. Lorem Ipsum has been the indust" +
                "ry's standard dummy text ever since the 1500s, when an unk" +
                "nown printer took a galley of type and scrambled it to mak" +
                "e a type specimen book. It has survived not only five cent" +
                "uries, but also the leap into electronic typesetting, rema" +
                "ining essentially unchanged. It was popularised in the 196" +
                "0s with the release of Letraset sheets containing Lorem Ip" +
                "sum passages, and more recently with desktop publishing so" +
                "ftware like Aldus PageMaker including versions of Lorem Ipsum.");
        printWriter.close();

        InputStream fileStream = new FileInputStream(inputFile);


        Client client = new Client();
        Socket c1  = client.start("127.0.0.1",8087);

        /*Socket c2  = client.start("127.0.0.1",8088);
        Socket c3  = client.start("127.0.0.1",8089);*/

        Socket[] connections = new Socket[]{c1, /*c2, c3*/};
        int len;
        while ((len = fileStream.available()) > 0){
            byte[] data = streamReader.read(fileStream, len);
            StreamBlock block = new StreamBlock("abcdefspace.test", data);
            for (Socket connection : connections){
                connection.getOutputStream().write(block.getFinalData());
            }
        }

        /*for (Socket connection : connections){
            connection.close();
        }*/




    }
}
