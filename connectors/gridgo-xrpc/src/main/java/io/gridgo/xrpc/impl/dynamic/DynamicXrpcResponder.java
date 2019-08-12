package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.connector.Producer;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.xrpc.XrpcMessageRegistry;
import io.gridgo.xrpc.XrpcResponder;

public interface DynamicXrpcResponder<KeyType> extends //
        XrpcMessageRegistry, //
        XrpcResponder<KeyType>, //
        ComponentLifecycle {

    void setFixedResponder(Producer fixedResponder);
}
