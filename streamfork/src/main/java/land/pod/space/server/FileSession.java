package land.pod.space.server;

import land.pod.space.Constant;
import land.pod.space.stream.StreamReader;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class FileSession implements Runnable {
    Socket client;
    public FileSession(Socket client) {
        this.client = client;
    }


    public static FileSession startNewSession(Socket client){
        return new FileSession(client);
    }

    @Override
    public void run() {
        int state = Constant.FILE_STATE_READ_NAME;
        try{
            InputStream is = client.getInputStream();
            FileOutputStream fos = null;
            while (!client.isClosed()) {
                while (state != Constant.FILE_STATE_CREATED && is.available() > 0) {
                    switch (state) {
                        case Constant.FILE_STATE_READ_NAME:
                            System.out.println("read file name state");
                            byte[] nameByte = new byte[16];
                            is.read(nameByte);
                            String name = new String(nameByte, StandardCharsets.UTF_8.name());
                            System.out.println(name);
                            fos = createFile(name);
                            state = Constant.FILE_STATE_READ_BODY;
                            break;
                        case Constant.FILE_STATE_READ_BODY:
                            System.out.println("read body of file");
                            byte[] bodyPart = StreamReader.read(is,
                                    Math.min(Constant.FILE_READ_BODY_BLOCK_SIZE, is.available()));
                            fos.write(bodyPart);
                            break;
                    }
                }
                if (state == Constant.FILE_STATE_READ_BODY) {
                    System.out.println("file created");
                    state = Constant.FILE_STATE_CREATED;
                    fos.flush();
                    fos.close();
                    client.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private FileOutputStream createFile(String name) throws IOException {
        System.out.println("creating file:"+ name);
        File parent = new File(Constant.FILE_BASE_DIR);
        if(!parent.exists())
            parent.mkdirs();

        File receivedFile = new File(parent, name);
        if(receivedFile.exists())
            receivedFile = new File(parent, name+"-"+ new Random().nextInt(1000));
        if(!receivedFile.createNewFile())
            throw new RuntimeException("can not create file on disk");
        return new FileOutputStream(receivedFile);
    }
}