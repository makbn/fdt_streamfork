import land.pod.space.streamfork.server.SFServer;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.apache.logging.log4j.core.config.properties.PropertiesConfiguration;
import org.apache.logging.log4j.core.config.xml.XmlConfiguration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestServer {

    private final static int TEST_SERVER_COUNT = 3;
    private final static int PORT_RANGE = 8050;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(TEST_SERVER_COUNT);
        for (int i = 0; i < TEST_SERVER_COUNT; i++) {
            int finalI = i;
            executorService.execute(() -> new SFServer().start("127.0.0.1", (PORT_RANGE + finalI), 100));
        }
    }
}
