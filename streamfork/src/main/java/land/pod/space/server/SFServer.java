package land.pod.space.server;

import land.pod.space.Constant;
import land.pod.space.stream.StreamReader;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SFServer {

    public void start(String address, int port, int queueSize){
        Executor exe = Executors.newCachedThreadPool();
        try (ServerSocket listener = new ServerSocket(port,queueSize, InetAddress.getByName(address))) {
            System.out.println("server is running at port:"+port);
            while (true) {
                Socket socket = listener.accept();
                print(socket);
                exe.execute(FileSession.startNewSession(socket));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void print(Socket socket) {
        System.out.println("new clinet accepted:"+socket.getPort());
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
