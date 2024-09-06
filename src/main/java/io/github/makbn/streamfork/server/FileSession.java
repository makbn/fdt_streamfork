package io.github.makbn.streamfork.server;

import io.github.makbn.streamfork.AppSettingsUtils;
import io.github.makbn.streamfork.common.FileState;
import io.github.makbn.streamfork.exception.FileSessionException;
import io.github.makbn.streamfork.exception.ProtocolException;
import io.github.makbn.streamfork.exception.WriteFileException;
import io.github.makbn.streamfork.stream.StreamReader;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

@Slf4j
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@AllArgsConstructor(staticName = "of")
public class FileSession implements Runnable {
    public static final int BOUND = 1000;
    Random random = new Random();
    @NonNull
    Socket client;
    @NonNull
    String baseDirectory;

    @Override
    public void run() {
        FileState state = FileState.FILE_STATE_READ_NAME_LEN;
        try {
            InputStream is = client.getInputStream();
            FileOutputStream fos = null;
            int nameLen = -1;
            int dataLen = -1;
            int readLen = 0;
            while (!client.isClosed()) {
                while ((state.compareTo(FileState.FILE_STATE_READ_BODY) < 0) || (readLen <= dataLen)) {
                    switch (state) {
                        case FILE_STATE_READ_NAME_LEN:
                            byte[] nameLenByte = new byte[AppSettingsUtils.FILE_NAME_LEN];
                            int len = is.read(nameLenByte);
                            if (len != AppSettingsUtils.FILE_NAME_LEN) {
                                throw ProtocolException.getInstance("name len is not standard");
                            }
                            String nameLenStr = new String(nameLenByte, StandardCharsets.UTF_8);
                            nameLen = Integer.parseInt(nameLenStr);
                            state = FileState.FILE_STATE_READ_NAME;
                            break;
                        case FILE_STATE_READ_NAME:
                            log.debug("read file name state");
                            byte[] nameByte = new byte[nameLen];
                            len = is.read(nameByte);
                            if (len != nameLen) {
                                throw ProtocolException.getInstance("name len is not standard");
                            }
                            String name = new String(nameByte, StandardCharsets.UTF_8);
                            fos = createFile(name);
                            state = FileState.FILE_STATE_READ_DATA_LEN;
                            break;
                        case FILE_STATE_READ_DATA_LEN:
                            byte[] dataLenByte = new byte[AppSettingsUtils.FILE_DATA_LEN];
                            len = is.read(dataLenByte);
                            if (len != AppSettingsUtils.FILE_DATA_LEN) {
                                throw ProtocolException.getInstance("data len is not standard");
                            }
                            String dataLenStr = new String(dataLenByte, StandardCharsets.UTF_8);
                            dataLen = Integer.parseInt(dataLenStr);
                            state = FileState.FILE_STATE_READ_BODY;
                            break;
                        case FILE_STATE_READ_BODY:
                            log.debug("read body of file");
                            int partSize = Math.min(AppSettingsUtils.FILE_READ_BODY_BLOCK_SIZE, is.available());
                            byte[] bodyPart = StreamReader.read(is, partSize);
                            readLen += partSize;
                            fos.write(bodyPart);
                            break;
                        default:
                            throw FileSessionException.getInstance("unknown state on file session");
                    }
                }
                if (state == FileState.FILE_STATE_READ_BODY) {
                    log.debug("file created");
                    state = FileState.FILE_STATE_CREATED;
                    fos.flush();
                    fos.close();
                    client.close();
                }
            }
        } catch (IOException e) {
            log.error("file session failed", e);
        }

    }

    private FileOutputStream createFile(String name) throws IOException {
        synchronized (FileState.class) {
            log.debug("creating file:{}", name);
            File parent = new File(baseDirectory);
            if (!parent.exists() && !parent.mkdirs())
                throw WriteFileException.getInstance("can not create parent folder on disk:" + baseDirectory);
            File receivedFile = new File(parent, name + "-" + random.nextInt(BOUND));
            if (receivedFile.exists())
                receivedFile = new File(parent, name + "-" + random.nextInt(BOUND));
            if (!receivedFile.createNewFile())
                throw WriteFileException.getInstance("can not create file on disk");
            return new FileOutputStream(receivedFile);
        }
    }
}
