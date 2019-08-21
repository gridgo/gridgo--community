package io.gridgo.xrpc.decorator.corrid;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.PrimitiveUtils;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.FieldNameDecorator;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import lombok.NonNull;

public class CorrIdTypeConverterSenderCodec extends FieldNameDecorator implements XrpcMessageCodec {

    @NonNull
    private final Class<?> targetType;

    public CorrIdTypeConverterSenderCodec(String fieldName, Class<?> targetType) {
        super(fieldName);
        this.targetType = targetType;
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        request.headers().computeIfPresent(getFieldName(), (key, value) -> {
            BValue asValue = value.asValue();
            asValue.setData(PrimitiveUtils.getValueFrom(targetType, asValue.getData()));
            return asValue;
        });
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        response.headers().computeIfPresent(getFieldName(), (key, value) -> {
            BValue asValue = value.asValue();
            asValue.setData(PrimitiveUtils.getValueFrom(targetType, asValue.getData()));
            return asValue;
        });
        return true;
    }
}
