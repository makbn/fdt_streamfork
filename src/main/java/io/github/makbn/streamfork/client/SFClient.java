package io.github.makbn.streamfork.client;

import io.github.makbn.streamfork.common.ClientEvent;
import io.github.makbn.streamfork.exception.ProtocolException;
import io.github.makbn.streamfork.stream.StreamBlock;
import io.github.makbn.streamfork.stream.StreamMode;
import io.github.makbn.streamfork.stream.StreamReader;
import io.github.makbn.streamfork.AppSettingsUtils;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SFClient {
    final List<Socket> connections;
    final StreamMode mode;
    boolean autoClosable = true;
    CountDownLatch parallelLatch;
    int maxParallelTimeout = -1;
    @Setter
    ClientListener clientListener;

    public SFClient(@NonNull StreamMode mode, ClientListener clientListener) {
        this.connections = new ArrayList<>();
        this.clientListener = clientListener;
        this.mode = mode;
    }

    private SFClient(@NonNull StreamMode mode) {
        this(mode, null);
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
        getClientListener().ifPresent(aListener -> aListener.onEvent(ClientEvent.BLOCK_RECEIVED));
        if (mode == StreamMode.SERIAL) {
            serialWrite(block);
        } else {
            parallelWrite(block);
        }
        if (autoClosable) {
            checkForClose();
        }
    }

    private void checkForClose() throws IOException, InterruptedException {
        log.info("closing client:");
        if (mode == StreamMode.SERIAL) {
            log.info("closing in serial mode");
            for (Socket s : connections) {
                s.close();
            }
        } else {
            log.info("closing in parallel mode! waiting for latch");
            if (maxParallelTimeout == -1)
                parallelLatch.await();
            else
                parallelLatch.await(maxParallelTimeout, TimeUnit.MILLISECONDS);

            log.info("latch triggered");
            for (Socket s : connections) {
                s.close();
            }
        }
        log.info("connections closed!");
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
        getClientListener().ifPresent(aListener -> aListener.onEvent(ClientEvent.BLOCK_COMPLETED));
    }

    private Socket checkSocket(Socket socket) throws IOException {
        if (socket.isClosed()) {
            return new Socket(socket.getInetAddress(), socket.getPort());
        }
        return socket;
    }

    private void parallelWrite(StreamBlock block) throws IOException, InterruptedException {
        parallelLatch = new CountDownLatch(connections.size());
        if (block.getType() == AppSettingsUtils.TYPE_CONTENT) {
            handleCWP(block);
        } else {
            handleSWP(block);
        }
    }

    private void handleSWP(StreamBlock block) throws IOException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(connections.size());
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
                        log.error("error on write", e);
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
        getClientListener().ifPresent(aListener -> aListener.onEvent(ClientEvent.BLOCK_COMPLETED));
    }

    private void handleCWP(StreamBlock block) throws InterruptedException {
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
                    log.error("error on write", e);
                }
            });
        }

        parallelLatch.await();
        getClientListener().ifPresent(aListener -> aListener.onEvent(ClientEvent.BLOCK_COMPLETED));
    }

    private Optional<ClientListener> getClientListener() {
        return Optional.ofNullable(clientListener);
    }
}
