package land.pod.space.streamfork;

public class AppSettingsUtils {
    public final static int FILE_NAME_LEN = 4;
    public final static int FILE_DATA_LEN = 12;
    public final static int FILE_STATE_READ_NAME_LEN = 0;
    public final static int FILE_STATE_READ_DATA_LEN = 1;
    public final static int FILE_STATE_READ_NAME = 2;
    public final static int FILE_STATE_READ_BODY = 3;
    public final static int FILE_STATE_CREATED = 4;
    public final static int FILE_READ_BODY_BLOCK_SIZE = 2048;
    public final static int TYPE_CONTENT = 0;
    public final static int TYPE_STREAM = 1;
    public final static String FILE_BASE_DIR = "files/";
}
