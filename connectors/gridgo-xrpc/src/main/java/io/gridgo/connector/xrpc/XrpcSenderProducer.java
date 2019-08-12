package io.gridgo.connector.xrpc;

import org.joo.promise4j.Promise;

import io.gridgo.connector.impl.AbstractProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcSender;

public class XrpcSenderProducer extends AbstractProducer {
    
    private XrpcSender sender;

    protected XrpcSenderProducer(ConnectorContext context, XrpcSender sender) {
        super(context);
        this.sender = sender;
    }

    @Override
    public boolean isCallSupported() {
        return true;
    }

    @Override
    public void send(Message message) {
        call(message);
    }

    @Override
    public Promise<Message, Exception> sendWithAck(Message message) {
        return call(message);
    }

    @Override
    public Promise<Message, Exception> call(Message message) {
        return sender.call(message);
    }

    @Override
    protected void onStart() {
        sender.start();
    }

    @Override
    protected void onStop() {
        sender.stop();
    }

    @Override
    protected String generateName() {
        return "xrpc.sender." + sender.getName();
    }
}
