package io.gridgo.xrpc.impl.dynamic;

import java.util.UUID;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CorrIdXrpcResponder extends EndpointDirectXrpcResponder {

    @Override
    public Deferred<Message, Exception> registerMessage(Message request) {
        var deferred = new CompletableDeferredObject<Message, Exception>();
        Payload payload = request.getPayload();
        BObject headers = payload.getHeaders();

        final var corrId = headers.getLong("corrId", -1);
        if (corrId < 0)
            throw new IllegalArgumentException("Request contains invalid corrId, expected long value which >= 0");

        final var replyTo = headers.getString("replyTo", null);

        deferred.promise() //
                .map(Message::ofAny) //
                .pipeFail(this::buildErrorMessage) //
                .then(msg -> {
                    msg.getPayload().addHeader("corrId", corrId);
                    sendResponse(replyTo, msg);
                    return Promise.of(null);
                })//
                .fail(ex -> {
                    log.error("Error caught while sending response", ex);
                });
        return deferred;
    }

    protected Promise<Message, Throwable> buildErrorMessage(Throwable ex) {
        Message response = Message.ofAny(ex == null ? "internal server error" : ex.getMessage());
        String traceId = UUID.randomUUID().toString();
        response.getPayload().addHeader("traceId", traceId);
        response.getPayload().addHeader("timestamp", System.currentTimeMillis());
        return Promise.of(response);
    }
}
