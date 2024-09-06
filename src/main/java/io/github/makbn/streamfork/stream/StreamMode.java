package io.github.makbn.streamfork.stream;

public enum StreamMode {
    /**
     * send data to all servers in parallel
     * */
    PARALLEL,
    /**
     * send data to server one by one
     * */
    SERIAL
}
