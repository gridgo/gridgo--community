package io.gridgo.xrpc.decorator.corrid;

import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.wrapCorrId;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.FieldNameDecorator;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import io.gridgo.xrpc.exception.XrpcException;

public class CorrIdReceiverCodec extends FieldNameDecorator implements XrpcMessageCodec {

    public CorrIdReceiverCodec(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = request.headers().remove(getFieldName());
        if (corrId == null)
            throw new XrpcException("corrId couldn't be found in request's header: " + request.getPayload().toBArray());

        if (!corrId.isValue())
            throw new XrpcException("invalid corrId in request, expected BValue, got: " + corrId);

        context.setCorrId(wrapCorrId(corrId));
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrId = context.getCorrId();
        response.headers().put(getFieldName(), BValue.of(corrId.getSource()));
        return true;
    }
}
