package io.github.makbn.streamfork.exception;

public class ProtocolException extends RuntimeException {

    public ProtocolException(String message) {
        super(message);
    }

    public static ProtocolException getInstance(String message){
        return new ProtocolException(message);
    }
}
