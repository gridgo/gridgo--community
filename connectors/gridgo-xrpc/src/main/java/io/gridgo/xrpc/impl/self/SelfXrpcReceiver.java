package io.gridgo.xrpc.impl.self;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Consumer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;
import lombok.NonNull;
import lombok.Setter;

public class SelfXrpcReceiver extends AbstractXrpcReceiver {

    @Setter
    private @NonNull SelfXrpcRequestUnpacker requestUnpacker = SelfXrpcRequestUnpacker.BODY;

    @Setter
    private @NonNull SelfXrpcResponsePacker responsePacker = SelfXrpcResponsePacker.BODY;

    @Override
    protected void onConsumerReady(Consumer consumer) {
        consumer.subscribe(this::onRequest);
    }

    private void onRequest(Message request, Deferred<Message, Exception> deferred) {
        var internalDeferred = new CompletableDeferredObject<BElement, Exception>();
        internalDeferred.always((stt, responseBody, ex) -> {
            if (stt == DeferredStatus.RESOLVED)
                deferred.resolve(responsePacker.pack(responseBody));
            else
                deferred.reject(ex);
        });

        this.publish(requestUnpacker.unpack(request), internalDeferred);
    }
}
