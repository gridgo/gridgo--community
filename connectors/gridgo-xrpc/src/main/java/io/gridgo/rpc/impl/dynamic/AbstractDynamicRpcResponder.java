package io.gridgo.rpc.impl.dynamic;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.ConnectorResolvableMessageRegistry;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractDynamicRpcResponder<KeyType> extends ConnectorResolvableMessageRegistry<Message, BElement>
        implements DynamicRpcResponder<KeyType> {

    @Setter
    @Getter(PROTECTED)
    private Producer fixedResponder;
}
