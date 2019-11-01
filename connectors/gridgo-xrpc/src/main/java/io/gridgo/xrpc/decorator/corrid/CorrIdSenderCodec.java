package io.gridgo.xrpc.decorator.corrid;

import static io.gridgo.utils.UuidUtils.bytesToUUID;
import static io.gridgo.utils.UuidUtils.getTimeFromUUID;
import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.generateCorrId;
import static io.gridgo.xrpc.decorator.corrid.CorrIdDecoratorHelper.wrapCorrId;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

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

    public static final int MAX_CORR_ID_TRY = 5;
    private final @NonNull Map<ByteArray, Deferred<Message, Exception>> deferredCache = new NonBlockingHashMap<>();

    public CorrIdSenderCodec(String fieldName) {
        super(fieldName);
    }

    @Override
    public boolean decorateRequest(XrpcRequestContext context, Message request) {

        var corrId = new AtomicReference<>(generateCorrId());

        var deferred = context.getDeferred();
        deferred.promise().always((stt, res, ex) -> deferredCache.remove(corrId.get()));

        int tryCount = 1;
        while (deferredCache.putIfAbsent(corrId.get(), deferred) != null) {
            tryCount++;
            if (tryCount > MAX_CORR_ID_TRY)
                return false;
            corrId.set(generateCorrId());
        }

        request.headers().putAny(getFieldName(), corrId.get().getSource());

        return true;
    }

    @Override
    public boolean decorateResponse(XrpcRequestContext context, Message response) {
        var corrIdHeader = response.headers().remove(getFieldName());
        if (corrIdHeader != null) {
            var corrId = wrapCorrId(corrIdHeader);
            var deferred = deferredCache.get(corrId);
            if (deferred == null) {
                if (log.isWarnEnabled())
                    log.warn("deferred cannot be found for corrId: {} --> request time: {}", corrId,
                            new Date(getTimeFromUUID(bytesToUUID(corrId.getSource()))));
            } else {
                context.setDeferred(deferred);
            }
        } else {
            log.warn("corrId cannot be found in response headers, response: {}", response.getPayload().toBArray());
        }

        return true;
    }
}
