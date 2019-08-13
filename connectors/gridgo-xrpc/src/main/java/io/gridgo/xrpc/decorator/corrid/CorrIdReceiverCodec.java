package io.gridgo.xrpc.decorator.corrid;

import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import io.gridgo.xrpc.exception.XrpcException;
import lombok.experimental.SuperBuilder;

@SuperBuilder
public class CorrIdReceiverCodec extends CorrIdReceiverDecorator implements XrpcMessageCodec {

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        response.headers().put(getFieldName(), context.getCorrId());
        return true;
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = request.headers().remove(getFieldName());
        if (corrId == null)
            throw new XrpcException("corrId not found in request's header");
        if (!corrId.isValue())
            throw new XrpcException("invalid corr id in request, expected BValue, got: " + corrId);
        context.setCorrId(corrId.asValue());
        return true;
    }
}
