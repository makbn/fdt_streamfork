package io.github.makbn.streamfork.common;

public enum FileState implements Comparable<FileState> {
    FILE_STATE_READ_NAME_LEN,
    FILE_STATE_READ_DATA_LEN,
    FILE_STATE_READ_NAME,
    FILE_STATE_READ_BODY,
    FILE_STATE_CREATED
}
