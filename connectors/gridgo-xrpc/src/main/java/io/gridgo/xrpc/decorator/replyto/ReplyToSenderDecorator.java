package io.gridgo.xrpc.decorator.replyto;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ReplyToSenderDecorator extends ReplyToDecorator implements XrpcRequestDecorator {

    private @NonNull String replyTo;

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        request.headers().putAny(getFieldName(), replyTo);
        return true;
    }
}
