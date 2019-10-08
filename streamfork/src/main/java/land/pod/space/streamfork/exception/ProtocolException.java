package land.pod.space.streamfork.exception;

public class ProtocolException extends RuntimeException {
    public ProtocolException() {
    }

    public ProtocolException(String message) {
        super(message);
    }

    public static ProtocolException getInstance(String message){
        return new ProtocolException(message);
    }
}
