package io.gridgo.xrpc.impl.self;

import io.gridgo.connector.Consumer;
import io.gridgo.xrpc.impl.AbstractXrpcReceiver;

public class SelfXrpcReceiver extends AbstractXrpcReceiver {

    @Override
    protected void onConsumerReady(Consumer consumer) {
        consumer.subscribe(this::publish);
    }
}
