package land.pod.space.server;

import land.pod.space.Constant;
import land.pod.space.stream.StreamReader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server {

    public void start(int port){
        try (ServerSocket listener = new ServerSocket(port)) {
            System.out.println("The date land.pod.space.server is running...");
            while (true) {
                try (Socket socket = listener.accept()) {
                    int state = Constant.FILE_STATE_READ_NAME;
                    InputStream is = socket.getInputStream();
                    FileOutputStream fos = null;
                    while (is.available() > 0){
                        switch (state){
                            case Constant.FILE_STATE_READ_NAME:
                                System.out.println("read file name state");
                                byte[] nameByte = new byte[16];
                                is.read(nameByte);
                                String name = new String(nameByte, StandardCharsets.UTF_8.name());
                                System.out.println(name);
                                fos = new FileOutputStream(Constant.FILE_BASE_DIR.concat(name));
                                state = Constant.FILE_STATE_READ_BODY;
                                break;
                            case Constant.FILE_STATE_READ_BODY:
                                System.out.println("read body of file");
                                byte[] bodyPart = StreamReader.read(is,
                                        Math.min(Constant.FILE_READ_BODY_BLOCK_SIZE,is.available()));
                                fos.write(bodyPart);
                                break;
                        }
                    }
                    if(state == Constant.FILE_STATE_READ_BODY) {
                        System.out.println("file created");
                        fos.flush();
                        fos.close();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String convertInputStreamToString(InputStream inputStream) throws IOException {

        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }

        return result.toString(StandardCharsets.UTF_8.name());

    }
}
