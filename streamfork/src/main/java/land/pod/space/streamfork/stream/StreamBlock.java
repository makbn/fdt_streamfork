package land.pod.space.streamfork.stream;

import land.pod.space.streamfork.AppSettingsUtils;

import java.io.IOException;
import java.io.InputStream;

public class StreamBlock {
    private byte[] data;
    private byte[] headerData;
    private String name;
    private InputStream inputStream;
    private int type;
    private byte[] finalData;

    public StreamBlock(String name, byte[] data) throws IOException {
        this.data = data;
        this.name = name;
        this.type = AppSettingsUtils.TYPE_CONTENT;
        constructFinalData();
    }

    public StreamBlock(String name, InputStream is) throws IOException {
        this.data = new byte[is.available()];
        this.type = AppSettingsUtils.TYPE_STREAM;
        this.name = name;
        this.inputStream = is;
        constructHeaderData();
    }

    private void constructHeaderData() throws IOException {
        byte[] nameBytes = name.getBytes();
        String nameLen = String.valueOf(nameBytes.length);

        while (nameLen.length() < AppSettingsUtils.FILE_NAME_LEN)
            nameLen = "0" + nameLen;

        String dataLen = String.valueOf(inputStream.available());
        while (dataLen.length() < AppSettingsUtils.FILE_DATA_LEN)
            dataLen = "0" + dataLen;

        headerData = new byte[nameBytes.length + AppSettingsUtils.FILE_NAME_LEN + AppSettingsUtils.FILE_DATA_LEN];
        System.arraycopy(nameLen.getBytes(), 0, headerData, 0, AppSettingsUtils.FILE_NAME_LEN);
        System.arraycopy(nameBytes, 0, headerData, AppSettingsUtils.FILE_NAME_LEN, nameBytes.length);
        System.arraycopy(dataLen.getBytes(), 0, headerData,
                AppSettingsUtils.FILE_NAME_LEN + nameBytes.length, AppSettingsUtils.FILE_DATA_LEN);
    }

    private void constructFinalData() throws IOException {
        constructHeaderData();
        finalData = new byte[headerData.length + data.length];
        System.arraycopy(headerData, 0, finalData, 0, headerData.length);
        System.arraycopy(data, 0, finalData, headerData.length, data.length);
    }

    public byte[] getFinalData() {
        return finalData;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public byte[] getHeaderData() {
        return headerData;
    }

    public int getType() {
        return type;
    }
}
