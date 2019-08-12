package io.gridgo.connector.xrpc.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;

public class XrpcConnectorUnitTest {

    private Connector senderConnector;

    private Connector receiverConnector;

    @Before
    public void setup() throws UnsupportedEncodingException {
        var factory = new DefaultConnectorFactory();

        var address = "localhost:8989";
        var replyAddress = "localhost:8888";

        var senderEndpoint = URLEncoder.encode("http2://" + address + "?method=POST&format=json", "utf-8");
        var replyTo = URLEncoder.encode("zmq:push:tcp://" + replyAddress, "utf-8");
        var replyEndpoint = URLEncoder.encode("zmq:pull:tcp://" + replyAddress, "utf-8");
        var senderConnectorEndpoint = "xrpc:sender:dynamic?endpoint=" + senderEndpoint + "&replyTo=" + replyTo
                + "&replyEndpoint=" + replyEndpoint;

        var receiverEndpoint = URLEncoder.encode("jetty:http://" + address, "utf-8");
        var receiverConnectorEndpoint = "xrpc:receiver:dynamic?endpoint=" + receiverEndpoint;

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
        Assert.assertEquals("testing", msg.body());
    }

    @After
    public void tearDown() {
        this.senderConnector.stop();
        this.receiverConnector.stop();
    }
}
