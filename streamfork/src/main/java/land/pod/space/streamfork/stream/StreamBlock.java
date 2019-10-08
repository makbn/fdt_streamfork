package land.pod.space.streamfork.stream;

import land.pod.space.streamfork.AppSettings;

import java.io.IOException;
import java.io.InputStream;

public class StreamBlock {
    private byte[] data;
    private String name;

    private byte[] finalData;

    public StreamBlock(String name, byte[] data) {
        this.data = data;
        this.name = name;
        constructFinalData();
    }

    public StreamBlock(InputStream is, String name) throws IOException {
        this.data = new byte[is.available()];
        is.read(this.data);
        this.name = name;
        constructFinalData();
    }

    private void constructFinalData() {
        byte[] nameBytes = name.getBytes();
        if (nameBytes.length != AppSettings.FILE_NAME_LEN) {
            throw new RuntimeException("file name len is not standard");
        }

        finalData = new byte[nameBytes.length + data.length];
        System.arraycopy(nameBytes, 0, finalData, 0, nameBytes.length);
        System.arraycopy(data, 0, finalData, nameBytes.length, data.length);
    }


    public byte[] getFinalData() {
        return finalData;
    }
}
