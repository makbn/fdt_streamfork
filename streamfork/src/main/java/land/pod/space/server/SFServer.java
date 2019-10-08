package land.pod.space.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class SFServer {
    private static Logger logger = LoggerFactory.getLogger(SFServer.class);
    private AtomicBoolean stop = new AtomicBoolean(false);

    public void start(String address, int port, int queueSize) {
        Executor exe = Executors.newCachedThreadPool();
        try (ServerSocket listener = new ServerSocket(port, queueSize, InetAddress.getByName(address))) {
            logger.info("server is running at port:" + port);
            while (!stop.get()) {
                Socket socket = listener.accept();
                print(socket);
                exe.execute(FileSession.startNewSession(socket));
            }
        } catch (IOException e) {
            logger.error("SFServer stopped", e);
        }
    }

    private void print(Socket socket) {
        logger.info("new client accepted:" + socket.getPort());
    }

    public void stop() {
        this.stop.set(true);
    }
}
