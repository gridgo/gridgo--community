package io.gridgo.rpc.impl.self;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.DeferredStatus;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Consumer;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.AbstractRpcReceiver;
import lombok.NonNull;
import lombok.Setter;

public class SelfRpcReceiver extends AbstractRpcReceiver {

    @Setter
    private @NonNull SelfRpcRequestUnpacker requestUnpacker = SelfRpcRequestUnpacker.BODY;

    @Setter
    private @NonNull SelfRpcResponsePacker responsePacker = SelfRpcResponsePacker.BODY;

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
