package io.github.makbn.streamfork;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AppSettingsUtils {
    public static final int FILE_NAME_LEN = 4;
    public static final int FILE_DATA_LEN = 12;
    public static final int FILE_READ_BODY_BLOCK_SIZE = 2048;
    public static final int TYPE_CONTENT = 0;
    public static final int TYPE_STREAM = 1;
}
