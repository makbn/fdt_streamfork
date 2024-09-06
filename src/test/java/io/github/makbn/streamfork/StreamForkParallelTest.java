package io.github.makbn.streamfork;

import io.github.makbn.streamfork.client.SFClient;
import io.github.makbn.streamfork.common.ClientEvent;
import io.github.makbn.streamfork.common.ServerEvent;
import io.github.makbn.streamfork.server.SFServer;
import io.github.makbn.streamfork.stream.StreamBlock;
import io.github.makbn.streamfork.stream.StreamMode;
import io.github.makbn.streamfork.stream.StreamReader;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.junit.jupiter.api.*;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.github.makbn.streamfork.TestHelper.ADDRESS;
import static io.github.makbn.streamfork.TestHelper.TEST_SERVER_COUNT;


@FieldDefaults(level = AccessLevel.PRIVATE)
class StreamForkParallelTest {
    final List<SFServer> servers = new ArrayList<>();
    final CountDownLatch startCountDownLatch = new CountDownLatch(TEST_SERVER_COUNT);
    final CountDownLatch stopCountDownLatch = new CountDownLatch(TEST_SERVER_COUNT);
    SFClient client;
    ExecutorService executorService;
    File dummyFile;
    String baseDir;
    int basePort = 49152;
    @BeforeEach
    public void setup() throws IOException, URISyntaxException, InterruptedException {
        baseDir = TestHelper.PARENT_PREFIX + UUID.randomUUID();
        basePort += new Random().nextInt(1000 - TEST_SERVER_COUNT);
        dummyFile = TestHelper.createTempDummyFile();
        executorService = Executors.newFixedThreadPool(TEST_SERVER_COUNT);
        for (int i = 0; i < TEST_SERVER_COUNT; i++) {
            final int finalI = i;
            final SFServer server = new SFServer(baseDir, event -> {
                if (event == ServerEvent.STARTED) {
                    startCountDownLatch.countDown();
                } else {
                    stopCountDownLatch.countDown();
                }
            });
            executorService.execute(() -> server.start(ADDRESS, (basePort + finalI), 100));
            servers.add(server);
        }
        // wait for executors to start servers

        boolean reached = startCountDownLatch.await(5, TimeUnit.SECONDS);

        if (!reached) {
            Assertions.fail("Servers did not start the expected amount of time");
        }
        client = SFClient.get(StreamMode.PARALLEL);
        for (int i = 0; i < TEST_SERVER_COUNT; i++) {
            client.addServer(ADDRESS, basePort + i);
        }

        client.setAutoClosable(true);
    }

    @AfterEach
    public void cleanup() throws IOException, InterruptedException {
        Files.delete(dummyFile.toPath());
        Path parent = new File(baseDir).toPath();
        try (Stream<Path> content = Files.walk(parent)) {
            content.filter(f -> !f.equals(parent)).forEach(generatedFile -> {
                try {
                    Files.deleteIfExists(generatedFile);
                } catch (IOException e) {
                    Assertions.fail(e);
                }
            });
        }
        Files.deleteIfExists(parent);
        servers.forEach(SFServer::stop);
        boolean reached = stopCountDownLatch.await(10, TimeUnit.SECONDS);
        if (!reached) {
            Assertions.fail("Servers did not stop the expected amount of time");
        }
        executorService.shutdown();
    }

    @RepeatedTest(5)
    void testSendFileToServer_readFromInputStream() throws IOException, InterruptedException {
        // Arrange
        InputStream fileStream = Files.newInputStream(dummyFile.toPath());
        String name = UUID.randomUUID().toString().substring(0, 16);
        StreamBlock block = new StreamBlock(name, fileStream);
        CountDownLatch latch = new CountDownLatch(1);
        client.setClientListener(clientEvent -> {
            if (clientEvent == ClientEvent.BLOCK_COMPLETED) {
                latch.countDown();
            }
        });
        // Act
        client.write(block);
        boolean reached = latch.await(1, TimeUnit.SECONDS);
        // we don't have ack step in the flow so when we close the client connection, it doesn't necessary meaning that
        // we created the file on the server side! for now let's wait for 10mill before checking the files
        Thread.sleep(10);
        if (!reached) {
            Assertions.fail("writing did not finish in the expected amount of time");
        }
        // Assert
        assertions(name);
    }

    @RepeatedTest(5)
    void testSendFileToServer_readFromArray() throws IOException, InterruptedException {
        // Arrange
        int len;
        byte[] data = null;
        InputStream fileStream = Files.newInputStream(dummyFile.toPath());
        while ((len = fileStream.available()) > 0){
            data = StreamReader.read(fileStream, len);
        }

        String name = UUID.randomUUID().toString().substring(0, 16);
        StreamBlock block = new StreamBlock(name, data);
        CountDownLatch latch = new CountDownLatch(1);
        client.setClientListener(clientEvent -> {
            if (clientEvent == ClientEvent.BLOCK_COMPLETED) {
                latch.countDown();
            }
        });
        // Act
        client.write(block);

        boolean reached = latch.await(1, TimeUnit.SECONDS);
        Thread.sleep(10);
        if (!reached) {
            Assertions.fail("writing did not finish in the expected amount of time");
        }

        // Assert
        assertions(name);
    }

    private void assertions(String name) throws IOException {
        File parent = new File(baseDir);
        Assertions.assertTrue(parent.exists());
        // each server writes a copy of file
        List<Path> files = new ArrayList<>();
        try (Stream<Path> generatedFilesStream = Files.walk(parent.toPath())) {
            generatedFilesStream.forEach(files::add);
        }
        Assertions.assertEquals(TEST_SERVER_COUNT, files.stream().filter(f -> f.getFileName().toString().contains(name)).count());
        String sourceFileContent = new BufferedReader(new InputStreamReader(Files.newInputStream(dummyFile.toPath()))).lines().collect(Collectors.joining("\n"));
        files.stream()
                .filter(f -> f.getFileName().toString().contains(name))
                .map(file -> {
                    String content = "";
                    try (Stream<String> lines = Files.lines(file)) {
                        content = lines.collect(Collectors.joining("\n"));
                    } catch (IOException e) {
                        Assertions.fail(e.getMessage());
                    }
                    return content;
                }).forEach(generatedContent -> Assertions.assertEquals(sourceFileContent, generatedContent, String.format("result: %s", generatedContent)));
        Assertions.assertTrue(true);
    }
}
