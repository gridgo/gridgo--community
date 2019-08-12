package io.gridgo.xrpc.impl.self;

import static lombok.AccessLevel.PROTECTED;

import org.joo.promise4j.Promise;

import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.impl.AbstractXrpcSender;
import lombok.Getter;

public class SelfXrpcSender extends AbstractXrpcSender {

    @Getter(PROTECTED)
    private Producer producer;

    @Override
    protected void onProducerReady(Producer producer) {
        if (!producer.isCallSupported()) {
            throw new RuntimeException("Call is not supported for endpoint: " + getEndpoint());
        }
        this.producer = producer;
    }

    @Override
    public Promise<Message, Exception> call(Message body) {
        return producer.call(body);
    }
}
