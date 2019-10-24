package io.gridgo.connector.xrpc.test;

import java.io.UnsupportedEncodingException;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.impl.SimpleRegistry;

public class XrpcConnectorUnitTest {

    private Connector senderConnector;

    private Connector receiverConnector;

    @Before
    public void setup() throws UnsupportedEncodingException {
        var address = "localhost:7788";
        var replyAddress = "localhost:8899";

        var registry = new SimpleRegistry();
        registry.register("senderEndpoint", "http2://" + address + "?method=POST&format=json")
                .register("replyTo", "zmq:push:tcp://" + replyAddress)
                .register("replyEndpoint", "zmq:pull:tcp://" + replyAddress)
                .register("receiverEndpoint", "jetty:http://" + address);

        var factory = new DefaultConnectorFactory();
        factory.setRegistry(registry);

        var senderConnectorEndpoint = "xrpc:sender:dynamic?encodeCorrIdToHex=true&endpointKey=senderEndpoint&replyToKey=replyTo&replyEndpointKey=replyEndpoint";

        var receiverConnectorEndpoint = "xrpc:receiver:dynamic?decodeCorrIdFromHex=true&endpointKey=receiverEndpoint";

        this.senderConnector = factory.createConnector(senderConnectorEndpoint);
        this.receiverConnector = factory.createConnector(receiverConnectorEndpoint);

        this.senderConnector.start();
        this.receiverConnector.start();
    }

    @Test
    public void testMixSendHttpReceiveZmq() throws PromiseException, InterruptedException {
        var consumer = this.receiverConnector.getConsumer().orElseThrow();
        var producer = this.senderConnector.getProducer().orElseThrow();
        consumer.subscribe((msg, deferred) -> {
            deferred.resolve(Message.ofAny("testing"));
        });
        var msg = producer.callAny(null).get();
        Assert.assertEquals("testing", msg.body().asValue().getString());
    }

    @After
    public void tearDown() {
        this.senderConnector.stop();
        this.receiverConnector.stop();
    }
}
