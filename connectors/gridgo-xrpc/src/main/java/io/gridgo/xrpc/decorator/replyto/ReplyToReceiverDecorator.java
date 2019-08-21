package io.gridgo.xrpc.decorator.replyto;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcRequestDecorator;
import io.gridgo.xrpc.exception.XrpcException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReplyToReceiverDecorator extends ReplyToDecorator implements XrpcRequestDecorator {

    public ReplyToReceiverDecorator(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var replyTo = request.headers().getString(getFieldName(), null);
        if (replyTo == null)
            throw new XrpcException("Reply to header (by key '" + getFieldName() + "') cannot be found in request");

        request.headers().remove(getFieldName());
        log.trace("[Receiver] receive replyTo from request: {}", replyTo);
        context.setReplyTo(replyTo);
        return true;
    }
}
