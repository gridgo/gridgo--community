package io.gridgo.rpc.fixed;

import static lombok.AccessLevel.PROTECTED;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.rpc.impl.AbstractRpcSender;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class FixedRpcSender extends AbstractRpcSender {

    @Getter(PROTECTED)
    private Producer producer;

    @Setter
    @Getter(PROTECTED)
    private @NonNull FixedRpcResponseUnpacker responseUnpacker = FixedRpcResponseUnpacker.DEFAULT;

    @Setter
    @Getter(PROTECTED)
    private @NonNull FixedRpcRequestPacker requestPacker = FixedRpcRequestPacker.DEFAULT;

    @Override
    protected void onProducerReady(Producer producer) {
        if (!producer.isCallSupported()) {
            throw new RuntimeException("Call is not supported for endpoint: " + getEndpoint());
        }
        this.producer = producer;
    }

    private Promise<BElement, Exception> processResponse(Message response) {
        return Promise.of(responseUnpacker.unpack(response));
    }

    @Override
    public Promise<BElement, Exception> send(BElement body) {
        return producer //
                .call(requestPacker.packRequest(body)) //
                .pipeDone(this::processResponse);
    }
}
