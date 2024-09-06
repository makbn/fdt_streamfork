package io.github.makbn.streamfork.server;

import io.github.makbn.streamfork.common.ServerEvent;

public interface ServerListener {
    void onEvent(ServerEvent serverEvent);
}
