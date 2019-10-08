package land.pod.space.streamfork.exception;

public class WriteFileException extends RuntimeException {

    public WriteFileException() {
    }

    public WriteFileException(String message) {
        super(message);
    }

    public static WriteFileException getInstance(String message){
        return new WriteFileException(message);
    }
}
