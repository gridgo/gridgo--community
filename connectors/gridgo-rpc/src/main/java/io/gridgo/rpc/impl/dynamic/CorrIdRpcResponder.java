package io.gridgo.rpc.impl.dynamic;

import static org.joo.promise4j.DeferredStatus.RESOLVED;

import java.util.UUID;

import org.joo.promise4j.Deferred;

import io.gridgo.bean.BElement;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class CorrIdRpcResponder extends EndpointDirectRpcResponder {

    @Override
    protected void prepareDeferred(Message request, BElement body, Deferred<BElement, Exception> deferred) {
        Payload payload = request.getPayload();
        BObject headers = payload.getHeaders();

        final var corrId = headers.getLong("corrId", -1);
        if (corrId < 0)
            throw new IllegalArgumentException("Request contains invalid corrId, expected long value which >= 0");

        final var replyTo = headers.getString("replyTo", null);
        
        deferred.promise().always((stt, responseBody, ex) -> {
            Message response;
            if (stt == RESOLVED) {
                response = Message.ofAny(responseBody);
            } else {
                response = Message.ofAny(ex == null ? "internal server error" : ex.getMessage());
                String traceId = UUID.randomUUID().toString();
                response.getPayload().addHeader("traceId", traceId);
                response.getPayload().addHeader("timestamp", System.currentTimeMillis());
            }

            response.getPayload().addHeader("corrId", corrId);
            sendResponse(replyTo, response);
        });
    }
}
