package io.gridgo.xrpc.decorator.corrid;

import java.util.function.Supplier;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@SuperBuilder
public class CorrIdSenderCodec extends CorrIdSenderDecorator implements XrpcMessageCodec {

    private @NonNull Supplier<BValue> idGenerator;

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = idGenerator.get();
        log.trace("[Sender] inject corrId to request: {}", corrId);

        var deferred = context.getDeferred();
        deferred.promise().always((stt, res, ex) -> {
            getDeferredCache().remove(corrId);
            if (ex != null) {
                log.error("Exception caught on sender promise", ex);
            }
        });

        getDeferredCache().put(corrId, deferred);
        request.headers().put(getFieldName(), corrId);
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrId = response.headers().getValue(getFieldName(), null);
        log.trace("[Sender] got corrId from response: {}", corrId);
        if (corrId != null) {
            var deferred = getDeferredCache().get(corrId);
            context.setDeferred(deferred);
        }
        return true;
    }
}
