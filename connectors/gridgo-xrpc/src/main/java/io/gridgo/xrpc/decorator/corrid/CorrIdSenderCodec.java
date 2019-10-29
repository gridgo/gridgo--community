package io.gridgo.xrpc.decorator.corrid;

import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.generateCorrId;
import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.wrapCorrId;

import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;
import org.joo.promise4j.Deferred;

import io.gridgo.framework.support.Message;
import io.gridgo.utils.wrapper.ByteArray;
import io.gridgo.xrpc.XrpcRequestContext;
import io.gridgo.xrpc.decorator.FieldNameDecorator;
import io.gridgo.xrpc.decorator.XrpcMessageCodec;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CorrIdSenderCodec extends FieldNameDecorator implements XrpcMessageCodec {

    private final @NonNull Map<ByteArray, Deferred<Message, Exception>> deferredCache = new NonBlockingHashMap<>();

    public CorrIdSenderCodec(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {
        var corrId = generateCorrId();

        var deferred = context.getDeferred();
        deferred.promise().always((stt, res, ex) -> {
            deferredCache.remove(corrId);
            if (ex != null)
                log.error("Exception caught on sender promise", ex);
        });

        deferredCache.put(corrId, deferred);
        request.headers().putAny(getFieldName(), corrId.getSource());
        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrId = response.headers().get(getFieldName());
        if (corrId != null)
            context.setDeferred(deferredCache.get(wrapCorrId(corrId)));
        return true;
    }
}
