package io.github.makbn.streamfork.stream;

import io.github.makbn.streamfork.AppSettingsUtils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import java.io.IOException;
import java.io.InputStream;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class StreamBlock {
    byte[] data;
    String name;
    @Getter
    int type;

    @Getter
    @NonFinal
    byte[] finalData;
    @Getter
    @NonFinal
    byte[] headerData;
    @Getter
    @NonFinal
    InputStream inputStream;

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
        StringBuilder nameLen = new StringBuilder(String.valueOf(nameBytes.length));

        while (nameLen.length() < AppSettingsUtils.FILE_NAME_LEN)
            nameLen.insert(0, "0");

        StringBuilder dataLen = new StringBuilder(String.valueOf(getDateLen()));
        while (dataLen.length() < AppSettingsUtils.FILE_DATA_LEN)
            dataLen.insert(0, "0");

        headerData = new byte[nameBytes.length + AppSettingsUtils.FILE_NAME_LEN + AppSettingsUtils.FILE_DATA_LEN];
        System.arraycopy(nameLen.toString().getBytes(), 0, headerData, 0, AppSettingsUtils.FILE_NAME_LEN);
        System.arraycopy(nameBytes, 0, headerData, AppSettingsUtils.FILE_NAME_LEN, nameBytes.length);
        System.arraycopy(dataLen.toString().getBytes(), 0, headerData,
                AppSettingsUtils.FILE_NAME_LEN + nameBytes.length, AppSettingsUtils.FILE_DATA_LEN);
    }

    private long getDateLen() throws IOException {
        if (inputStream != null) {
            return inputStream.available();
        } else {
            return data.length;
        }
    }

    private void constructFinalData() throws IOException {
        constructHeaderData();
        finalData = new byte[headerData.length + data.length];
        System.arraycopy(headerData, 0, finalData, 0, headerData.length);
        System.arraycopy(data, 0, finalData, headerData.length, data.length);
    }

}
