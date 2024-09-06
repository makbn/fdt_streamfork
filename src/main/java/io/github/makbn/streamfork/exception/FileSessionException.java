package io.github.makbn.streamfork.exception;

public class FileSessionException extends RuntimeException {

    public FileSessionException(String message) {
        super(message);
    }

    public static FileSessionException getInstance(String message){
        return new FileSessionException(message);
    }
}
