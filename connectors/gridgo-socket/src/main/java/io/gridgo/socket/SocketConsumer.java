package io.gridgo.socket;

import io.gridgo.connector.Consumer;
import io.gridgo.connector.HasResponder;
import io.gridgo.socket.impl.DefaultSocketConsumer;
import io.gridgo.socket.impl.DefaultSocketConsumer.SocketConsumerBuidler;

public interface SocketConsumer extends Consumer, HasResponder, HasBindingPort {

    public final int DEFAULT_RECV_TIMEOUT = 100; // receive timeout apply on polling event loop

    static SocketConsumerBuidler builder() {
        return DefaultSocketConsumer.builder();
    }

    long getTotalRecvBytes();

    long getTotalRecvMessages();

}
