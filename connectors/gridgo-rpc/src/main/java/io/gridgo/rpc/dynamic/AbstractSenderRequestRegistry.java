package io.gridgo.rpc.dynamic;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.AbstractMessageRegistry;

public abstract class AbstractSenderRequestRegistry extends AbstractMessageRegistry<BElement, Message>
        implements DynamicRpcSenderRequestRegistry {

    @Override
    protected Message translateMessage(BElement request) {
        return Message.ofAny(request);
    }
}
