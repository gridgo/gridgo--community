package io.gridgo.xrpc.test.dynamic;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;
import io.gridgo.xrpc.test.AbstractRPCTest;

public class TestZmqRPCPushPull extends AbstractRPCTest {

    private XrpcSender sender;
    private XrpcReceiver receiver;

    @Before
    public void setup() {
        String address = "localhost:8989";
        String replyAddress = "localhost:8888";
        sender = getRpcBuilder().dynamicSender() //
                .endpoint("zmq:push:tcp://" + address) //
                .replyTo("zmq:push:tcp://" + replyAddress) //
                .replyEndpoint("zmq:pull:tcp://" + replyAddress) //
                .build();

        receiver = getRpcBuilder().dynamicReceiver()//
                .endpoint("zmq:pull:tcp://" + address) //
                .build();

        sender.start();
        receiver.start();
    }

    @After
    public void tearDown() {
        this.sender.stop();
        this.receiver.stop();
    }

    @Test
    public void testEcho() throws PromiseException, InterruptedException {
        this.receiver.subscribe((requestBody, deferred) -> deferred.resolve(requestBody));

        var origin = BValue.of("this is test text");
        var response = this.sender.call(origin).get();

        assertEquals(origin, response);
    }
}
