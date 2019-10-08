import land.pod.space.client.SFClient;
import land.pod.space.stream.StreamBlock;
import land.pod.space.stream.StreamMode;
import land.pod.space.stream.StreamReader;

import java.io.*;
import java.util.UUID;

/**
 * Mehdi AKbarian-astaghi 10/6/19
 */
public class TestClient {

    /**
     * {{@link TestServer}} should be started!
     *
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
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

        SFClient client = SFClient.get(StreamMode.Parallel)
                .addServer("127.0.0.1", 8050)
                .addServer("127.0.0.1", 8051)
                .addServer("127.0.0.1", 8052)
                .setAutoClosable(true);


        int len;
        byte[] data = null;
        while ((len = fileStream.available()) > 0) {
            data = StreamReader.read(fileStream, len);
        }

        String name = UUID.randomUUID().toString().substring(0, 16);
        StreamBlock block = new StreamBlock(name, data);
        client.write(block);

        System.exit(0);
    }
}
