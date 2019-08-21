package io.gridgo.xrpc.decorator.corrid;

import io.gridgo.bean.BType;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.FieldNameDecorator;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;

public class CorrIdHexSenderCodec extends FieldNameDecorator implements XrpcMessageCodec {

    public CorrIdHexSenderCodec(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = request.headers().get(this.getFieldName());
        if (corrId != null && corrId.getType() == BType.RAW)
            corrId.asValue().encodeHex();
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrId = response.headers().get(this.getFieldName());
        if (corrId != null && corrId.getType() == BType.STRING)
            corrId.asValue().decodeHex();
        return true;
    }
}
