package land.pod.space.client;

import java.io.IOException;
import java.net.Socket;

public class Client {

    public Socket start(String host, int port) throws IOException {
        Socket socket = new Socket( host, port);
        return socket;
    }
}
