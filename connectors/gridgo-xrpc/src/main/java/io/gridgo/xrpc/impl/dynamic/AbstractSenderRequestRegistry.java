package io.gridgo.xrpc.impl.dynamic;

import static lombok.AccessLevel.PROTECTED;

import io.gridgo.bean.BElement;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.impl.AbstractMessageRegistry;
import lombok.Getter;
import lombok.Setter;

public abstract class AbstractSenderRequestRegistry extends AbstractMessageRegistry<BElement, Message>
        implements DynamicXrpcSenderRequestRegistry {

    @Setter
    @Getter(PROTECTED)
    private String replyTo;

    @Override
    protected Message translateMessage(BElement request) {
        Message message = Message.ofAny(request);
        if (replyTo != null) {
            message.getPayload().addHeader("replyTo", replyTo);
        }
        return message;
    }
}
