package land.pod.space.streamfork.server;

import land.pod.space.streamfork.AppSettingsUtils;
import land.pod.space.streamfork.exception.FileSessionException;
import land.pod.space.streamfork.exception.ProtocolException;
import land.pod.space.streamfork.exception.WriteFileException;
import land.pod.space.streamfork.stream.StreamReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class FileSession implements Runnable {
    private static Logger logger = LogManager.getLogger(FileSession.class.getName());
    private Socket client;

    private FileSession(Socket client) {
        this.client = client;
    }

    static FileSession startNewSession(Socket client) {
        return new FileSession(client);
    }

    @Override
    public void run() {
        int state = AppSettingsUtils.FILE_STATE_READ_NAME_LEN;
        try {
            InputStream is = client.getInputStream();
            FileOutputStream fos = null;
            int nameLen = -1;
            int dataLen = -1;
            int readLen = 0;
            while (!client.isClosed()) {
                while ((state < AppSettingsUtils.FILE_STATE_READ_BODY)
                        || (readLen <= dataLen)) {
                    switch (state) {
                        case AppSettingsUtils.FILE_STATE_READ_NAME_LEN:
                            byte[] nameLenByte = new byte[AppSettingsUtils.FILE_NAME_LEN];
                            int len = is.read(nameLenByte);
                            if (len != AppSettingsUtils.FILE_NAME_LEN)
                                throw ProtocolException.getInstance("name len is not standard");
                            String nameLenStr = new String(nameLenByte, StandardCharsets.UTF_8.name());
                            nameLen = Integer.valueOf(nameLenStr);
                            state = AppSettingsUtils.FILE_STATE_READ_NAME;
                            break;
                        case AppSettingsUtils.FILE_STATE_READ_NAME:
                            logger.info("read file name state");
                            byte[] nameByte = new byte[nameLen];
                            len = is.read(nameByte);
                            if (len != nameLen)
                                throw ProtocolException.getInstance("name len is not standard");
                            String name = new String(nameByte, StandardCharsets.UTF_8.name());
                            fos = createFile(name);
                            state = AppSettingsUtils.FILE_STATE_READ_DATA_LEN;
                            break;
                        case AppSettingsUtils.FILE_STATE_READ_DATA_LEN:
                            byte[] dataLenByte = new byte[AppSettingsUtils.FILE_DATA_LEN];
                            len = is.read(dataLenByte);
                            if (len != AppSettingsUtils.FILE_DATA_LEN)
                                throw ProtocolException.getInstance("data len is not standard");
                            String dataLenStr = new String(dataLenByte, StandardCharsets.UTF_8.name());
                            dataLen = Integer.valueOf(dataLenStr);
                            state = AppSettingsUtils.FILE_STATE_READ_BODY;
                            break;
                        case AppSettingsUtils.FILE_STATE_READ_BODY:
                            logger.info("read body of file");
                            int partSize = Math.min(AppSettingsUtils.FILE_READ_BODY_BLOCK_SIZE, is.available());
                            byte[] bodyPart = StreamReader.read(is, partSize);
                            readLen += partSize;
                            fos.write(bodyPart);
                            break;
                        default:
                            throw FileSessionException.getInstance("unknown state on file session");
                    }
                }
                if (state == AppSettingsUtils.FILE_STATE_READ_BODY) {
                    logger.info("file created");
                    state = AppSettingsUtils.FILE_STATE_CREATED;
                    fos.flush();
                    fos.close();
                    client.close();
                }
            }
        } catch (IOException e) {
            logger.error("file session failed", e);
        }

    }

    private FileOutputStream createFile(String name) throws IOException {
        logger.info("creating file:" + name);
        File parent = new File(AppSettingsUtils.FILE_BASE_DIR);
        if (!parent.exists() && !parent.mkdirs())
            throw WriteFileException.getInstance("can not create parent folder on disk:"+ AppSettingsUtils.FILE_BASE_DIR);
        File receivedFile = new File(parent, name + "-" + new Random().nextInt(1000));
        if (receivedFile.exists())
            receivedFile = new File(parent, name + "-" + new Random().nextInt(1000));
        if (!receivedFile.createNewFile())
            throw WriteFileException.getInstance("can not create file on disk");
        return new FileOutputStream(receivedFile);
    }
}
