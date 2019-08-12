package io.gridgo.xrpc.impl.dynamic;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.framework.ComponentLifecycle;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcMessageRegistry;
import io.gridgo.xrpc.XrpcResponder;

public interface DynamicXrpcResponder<KeyType> extends //
        XrpcMessageRegistry<Message, BElement>, //
        XrpcResponder<KeyType>, //
        ComponentLifecycle {

    void setFixedResponder(Producer fixedResponder);
}
