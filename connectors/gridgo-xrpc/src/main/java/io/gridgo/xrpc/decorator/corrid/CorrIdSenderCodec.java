package io.gridgo.xrpc.decorator.corrid;

import java.util.Map;
import java.util.function.Supplier;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CorrIdSenderCodec extends CorrIdSenderDecorator implements XrpcMessageCodec {

    private final @NonNull Supplier<BValue> idGenerator;

    public CorrIdSenderCodec(String fieldName, Map<BValue, Deferred<Message, Exception>> deferredCache,
            Supplier<BValue> idGenerator) {
        super(fieldName, deferredCache);
        this.idGenerator = idGenerator;
    }

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
