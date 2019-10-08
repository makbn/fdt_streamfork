package land.pod.space.streamfork.exception;

public class FileSessionException extends RuntimeException {
    public FileSessionException() {
    }

    public FileSessionException(String message) {
        super(message);
    }

    public static FileSessionException getInstance(String message){
        return new FileSessionException(message);
    }
}
