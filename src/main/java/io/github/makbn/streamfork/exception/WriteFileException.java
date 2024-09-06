package io.github.makbn.streamfork.exception;

public class WriteFileException extends RuntimeException {

    public WriteFileException(String message) {
        super(message);
    }

    public static WriteFileException getInstance(String message){
        return new WriteFileException(message);
    }
}
