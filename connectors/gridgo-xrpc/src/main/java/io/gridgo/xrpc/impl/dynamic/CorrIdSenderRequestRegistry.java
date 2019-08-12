package io.gridgo.xrpc.impl.dynamic;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.cliffc.high_scale_lib.NonBlockingHashMapLong;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class CorrIdSenderRequestRegistry extends AbstractSenderRequestRegistry {

    private static final AtomicLong CORR_ID_SEED = new AtomicLong(0);

    private final Map<Long, Deferred<Message, Exception>> deferreds = new NonBlockingHashMapLong<>();

    @Override
    public Deferred<Message, Exception> registerMessage(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        final long corrId = CORR_ID_SEED.getAndIncrement();
        request.getPayload().addHeader("corrId", corrId);
        deferreds.put(corrId, deferred);
        deferred.promise().always((stt, res, ex) -> {
            deferreds.remove(corrId);
        });
        return deferred;
    }

    @Override
    public void handleResponse(Message response) {
        Payload payload = response.getPayload();
        final long corrId = payload.getHeaders().getLong("corrId", -1l);
        var deferred = deferreds.get(corrId);
        if (deferred != null) {
            deferred.resolve(response);
        }
    }
}
