package land.pod.space.client;

import land.pod.space.stream.StreamBlock;
import land.pod.space.stream.StreamMode;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SFClient {

    private ArrayList<Socket> connections;
    private StreamMode mode;
    private boolean autoClosable = true;
    private CountDownLatch parallelLatch;
    private int maxParallelTimeout = -1;

    public static SFClient get(StreamMode mode){
        return new SFClient(mode);
    }

    private SFClient(StreamMode mode) {
        this.connections = new ArrayList<>();
        this.mode = mode;
    }

    public SFClient setMaxParallelTimeout(int maxParallelTimeout) {
        this.maxParallelTimeout = maxParallelTimeout;
        return this;
    }

    public SFClient addServer(String host, int port) throws IOException {
        Socket socket = new Socket( host, port);
        connections.add(socket);
        return this;
    }

    public SFClient setAutoClosable(boolean autoClosable) {
        this.autoClosable = autoClosable;
        return this;
    }

    public void write(StreamBlock block) throws IOException, InterruptedException {
        if (mode == StreamMode.Serial){
            serialWrite(block);
        }else {
            parallelWrite(block);
        }
        checkForClose();
    }

    private void checkForClose() throws IOException, InterruptedException {
        System.out.print("closing client");
        if (mode == StreamMode.Serial){
            System.out.println(" in serial mode");
            for (Socket s : connections){
                s.close();
            }
        }else {
            System.out.println(" in parallel mode! waiting for latch");
            if(maxParallelTimeout == -1)
                parallelLatch.await();
            else
                parallelLatch.await(maxParallelTimeout, TimeUnit.MILLISECONDS);

            System.out.println("latch triggered");
            for (Socket s : connections){
                s.close();
            }
        }

        System.out.println("connections closed!");
    }


    public void close() throws IOException {
        try {
            checkForClose();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void serialWrite(StreamBlock block) throws IOException {
        for (Socket socket: connections){
            socket = checkSocket(socket);
            OutputStream os = socket.getOutputStream();
            os.write(block.getFinalData());
            os.flush();
            os.close();
        }
    }

    private Socket checkSocket(Socket socket) throws IOException {
        if(socket.isClosed()){
            socket = new Socket(socket.getInetAddress(), socket.getPort());
        }
        return socket;
    }

    private void parallelWrite(StreamBlock block){
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
        ParallelExceptionCallback callback = e -> {
            Thread t = Thread.currentThread();
            t.getUncaughtExceptionHandler().uncaughtException(t, e);
        };
        parallelLatch = new CountDownLatch(connections.size());
        for (Socket socket: connections){
            executorService.execute(()->{
                try {
                    final Socket fSocket = checkSocket(socket);
                    OutputStream os = fSocket.getOutputStream();
                    os.write(block.getFinalData());
                    os.flush();
                    os.close();
                    parallelLatch.countDown();
                } catch (IOException e) {
                    callback.catchException(e);
                }
            });
        }
    }

    public void write(String name, byte[] data) throws IOException, InterruptedException {
        write(new StreamBlock(name, data));
    }


}
