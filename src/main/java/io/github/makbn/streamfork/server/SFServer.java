package io.github.makbn.streamfork.server;

import io.github.makbn.streamfork.common.ServerEvent;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class SFServer {
    AtomicBoolean stop = new AtomicBoolean(false);
    ServerListener serverListener;
    @NonFinal
    ServerSocket serverSocket;
    @NonNull
    String baseDirectory;
    public SFServer(@NonNull String baseDirectory, ServerListener serverListener) {
        this.baseDirectory = baseDirectory;
        this.serverListener = serverListener;
    }

    public SFServer(@NonNull String baseDirectory ) {
        this(baseDirectory, null);
    }

    public void start(@NonNull String address, int port, int queueSize) {
        ExecutorService exe = Executors.newCachedThreadPool();
        try (ServerSocket closable = new ServerSocket(port, queueSize, InetAddress.getByName(address))) {
            serverSocket = closable;
            log.info("server is running at port: {}", port);
            getServerListener().ifPresent(aListener -> aListener.onEvent(ServerEvent.STARTED));
            while (!stop.get()) {
                Socket socket = closable.accept();
                print(socket);
                exe.execute(FileSession.of(socket, baseDirectory));
            }
        } catch (IOException e) {
            log.error("SFServer stopped", e);
        }finally {
            exe.shutdown();
            getServerListener().ifPresent(aListener -> aListener.onEvent(ServerEvent.STOPPED));
        }

    }

    private void print(Socket socket) {
        log.debug("new client accepted: {}", socket.getPort());
    }

    public void stop() {
        this.stop.set(true);
        getServerSocket().ifPresent(aServerSocket -> {
            try {
                aServerSocket.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        });
    }


    private Optional<ServerListener> getServerListener() {
        return Optional.ofNullable(serverListener);
    }

    private Optional<ServerSocket> getServerSocket() {
        return Optional.ofNullable(serverSocket);
    }
}
