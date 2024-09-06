package io.github.makbn.streamfork.client;


import io.github.makbn.streamfork.common.ClientEvent;

public interface ClientListener {
    void onEvent(ClientEvent clientEvent);
}
