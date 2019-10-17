package io.gridgo.xrpc.decorator.replyto;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReplyToSenderDecorator extends ReplyToDecorator implements XrpcRequestDecorator {

    private final @NonNull String replyTo;

    public ReplyToSenderDecorator(String fieldName, String replyTo) {
        super(fieldName);
        this.replyTo = replyTo;
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        log.trace("[Sender] inject replyTo to request: {}", replyTo);
        request.headers().putAny(getFieldName(), replyTo);
        return true;
    }
}
