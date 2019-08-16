package io.gridgo.xrpc.decorator.corrid;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import io.gridgo.xrpc.exception.XrpcException;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
public class CorrIdReceiverCodec extends CorrIdReceiverDecorator implements XrpcMessageCodec {

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = request.headers().remove(getFieldName());
        log.trace("[Receiver] receive corrId from request: {}", corrId);
        if (corrId == null)
            throw new XrpcException("corrId not found in request's header");
        if (!corrId.isValue())
            throw new XrpcException("invalid corr id in request, expected BValue, got: " + corrId);
        context.setCorrId(corrId.asValue());
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrId = context.getCorrId();
        log.trace("[Receiver] inject corrId to response: {}", corrId);
        response.headers().put(getFieldName(), corrId);
        return true;
    }
}
