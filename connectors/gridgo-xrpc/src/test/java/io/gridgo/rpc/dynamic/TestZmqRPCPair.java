package io.gridgo.rpc.dynamic;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.rpc.AbstractRPCTest;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;

public class TestZmqRPCPair extends AbstractRPCTest {

    private XrpcSender sender;
    private XrpcReceiver receiver;

    @Before
    public void setup() {
        String address = "localhost:8989";
        sender = getRpcBuilder().dynamicSender()//
                .endpoint("zmq:pair:tcp:connect://" + address) //
                .build();

        receiver = getRpcBuilder().dynamicReceiver() //
                .endpoint("zmq:pair:tcp:bind://" + address) //
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
        var response = this.sender.send(origin).get();

        assertEquals(origin, response);
    }
}
