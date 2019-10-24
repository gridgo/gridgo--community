package io.gridgo.connector.xrpc;

import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.support.SubscriberDisposable;

public class XrpcReceiverConsumer extends AbstractConsumer {

    private XrpcReceiver receiver;

    private SubscriberDisposable disposable;

    protected XrpcReceiverConsumer(ConnectorContext context, XrpcReceiver receiver) {
        super(context);
        this.receiver = receiver;
    }

    @Override
    protected void onStart() {
        this.disposable = receiver.subscribe(this::publish);
        receiver.start();
    }

    @Override
    protected void onStop() {
        receiver.stop();
        this.disposable.dispose();
    }

    @Override
    protected String generateName() {
        return "xrpc.receiver." + receiver.getName();
    }
}
