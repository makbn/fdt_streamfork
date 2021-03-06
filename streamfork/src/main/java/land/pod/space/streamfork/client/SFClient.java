package land.pod.space.streamfork.client;

import land.pod.space.streamfork.AppSettingsUtils;
import land.pod.space.streamfork.exception.ProtocolException;
import land.pod.space.streamfork.stream.StreamBlock;
import land.pod.space.streamfork.stream.StreamMode;
import land.pod.space.streamfork.stream.StreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SFClient {
    private static Logger logger = LogManager.getLogger(SFClient.class.getName());
    private ArrayList<Socket> connections;
    private StreamMode mode;
    private boolean autoClosable = true;
    private CountDownLatch parallelLatch;
    private int maxParallelTimeout = -1;

    private SFClient(StreamMode mode) {
        this.connections = new ArrayList<>();
        this.mode = mode;
    }

    public static SFClient get(StreamMode mode) {
        return new SFClient(mode);
    }

    public SFClient setMaxParallelTimeout(int maxParallelTimeout) {
        this.maxParallelTimeout = maxParallelTimeout;
        return this;
    }

    public SFClient addServer(String host, int port) throws IOException {
        Socket socket = new Socket(host, port);
        connections.add(socket);
        return this;
    }

    public SFClient setAutoClosable(boolean autoClosable) {
        this.autoClosable = autoClosable;
        return this;
    }

    public void write(StreamBlock block) throws IOException, InterruptedException {
        if (mode == StreamMode.Serial) {
            serialWrite(block);
        } else {
            parallelWrite(block);
        }
        if (autoClosable) {
            checkForClose();
        }
    }

    private void checkForClose() throws IOException, InterruptedException {
        logger.info("closing client:");
        if (mode == StreamMode.Serial) {
            logger.info("closing in serial mode");
            for (Socket s : connections) {
                s.close();
            }
        } else {
            logger.info("closing in parallel mode! waiting for latch");
            if (maxParallelTimeout == -1)
                parallelLatch.await();
            else
                parallelLatch.await(maxParallelTimeout, TimeUnit.MILLISECONDS);

            logger.info("latch triggered");
            for (Socket s : connections) {
                s.close();
            }
        }

        logger.info("connections closed!");
    }


    public void close() throws IOException {
        try {
            checkForClose();
        } catch (InterruptedException e) {
            logger.error("exception on closing SFClient", e);
        }
    }

    private void serialWrite(StreamBlock block) throws IOException {
        if (block.getType() == AppSettingsUtils.TYPE_STREAM)
            throw ProtocolException.getInstance("stream and serial are not compatible");
        for (Socket socket : connections) {
            socket = checkSocket(socket);
            OutputStream os = socket.getOutputStream();
            os.write(block.getFinalData());
            os.flush();
            os.close();
        }
    }

    private Socket checkSocket(Socket socket) throws IOException {
        if (socket.isClosed()) {
            return new Socket(socket.getInetAddress(), socket.getPort());
        }
        return socket;
    }

    private void parallelWrite(StreamBlock block) throws IOException, InterruptedException {
        if (block.getType() == AppSettingsUtils.TYPE_CONTENT) {
            handleCWP(block);
        } else {
            handleSWP(block);
        }
    }

    private void handleSWP(StreamBlock block) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
        parallelLatch = new CountDownLatch(connections.size());
        ArrayList<OutputStream> oss = new ArrayList<>();
        ArrayList<Socket> stableSockets = new ArrayList<>();
        for (Socket socket : connections) {
            stableSockets.add(checkSocket(socket));
        }
        for (Socket socket : stableSockets) {
            OutputStream os = socket.getOutputStream();
            os.write(block.getHeaderData());
            oss.add(os);
        }

        InputStream is = block.getInputStream();
        int len;
        while ((len = is.available()) > 0) {
            byte[] part = StreamReader.read(is,
                    Math.min(len, AppSettingsUtils.FILE_READ_BODY_BLOCK_SIZE));
            parallelLatch = new CountDownLatch(connections.size());
            for (OutputStream os : oss) {
                executorService.execute(() -> {
                    try {
                        os.write(part);
                        parallelLatch.countDown();
                    } catch (IOException e) {
                        logger.error("error on write", e);
                    }
                });
            }
            parallelLatch.await();
        }

        for (OutputStream os : oss) {
            os.flush();
            os.close();
        }

        for (Socket ss : stableSockets)
            ss.close();
    }

    private void handleCWP(StreamBlock block) {
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
        for (Socket socket : connections) {
            executorService.execute(() -> {
                try {
                    final Socket fSocket = checkSocket(socket);
                    OutputStream os = fSocket.getOutputStream();
                    os.write(block.getFinalData());
                    os.flush();
                    os.close();
                    parallelLatch.countDown();
                } catch (IOException e) {
                    logger.error("error on write", e);
                }
            });
        }
    }

    public void write(String name, byte[] data) throws IOException, InterruptedException {
        write(new StreamBlock(name, data));
    }


}
