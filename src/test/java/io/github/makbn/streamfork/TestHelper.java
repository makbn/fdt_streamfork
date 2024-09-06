package io.github.makbn.streamfork;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestHelper {
    public final static String PARENT_PREFIX = "files-";
    public static final int TEST_SERVER_COUNT = 3;
    public static final String ADDRESS = "127.0.0.1";

    public static File createTempDummyFile() throws IOException, URISyntaxException {
        File inputFile = File.createTempFile("temp", "txt");
        // Get the resource from the test classloader
        Path path = Paths.get(Objects.requireNonNull(TestHelper.class.getClassLoader().getResource("test_file_content.txt")).toURI());

        // Read all lines from the file
        try (Stream<String> lines = Files.lines(path)) {
            String fileContent = lines.collect(Collectors.joining("\n"));
            FileWriter fileWriter = new FileWriter(inputFile);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(fileContent);
            printWriter.close();
        }

        return inputFile;
    }
}
