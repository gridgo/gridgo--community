package io.gridgo.xrpc.impl.dynamic;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.impl.ConnectorResolvableMessageRegistry;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractDynamicXrpcResponder<KeyType> extends ConnectorResolvableMessageRegistry<Message, BElement>
        implements DynamicXrpcResponder<KeyType> {

    @Setter
    @Getter(PROTECTED)
    private Producer fixedResponder;
}
