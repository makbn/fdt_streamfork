import stream.StreamReader;

import java.io.*;

/**
 * Mehdi AKbarian-astaghi 10/6/19
 */
public class TestStream {

    public static void main(String[] args) throws IOException {
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


        File[] copies = new File[]{new File("copy1.txt"),
                new File("copy2.txt"),
                new File("copy3.txt")};

        for(File copy : copies)
            copy.createNewFile();

        FileOutputStream[] outputStreams = new FileOutputStream[]{new FileOutputStream(copies[0]),
                new FileOutputStream(copies[1]),
                new FileOutputStream(copies[2])};

        int len;
        while ((len = fileStream.available()) > 0){
            byte[] data = streamReader.read(fileStream, Math.min(len, 2048));
            for (FileOutputStream fos : outputStreams){
                fos.write(data);
                System.out.println("write to:" + fos.hashCode());
            }
        }

        for (FileOutputStream fos : outputStreams){
            fos.flush();
            fos.close();
        }

    }
}
