package io.gridgo.xrpc.test.fixed;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;
import io.gridgo.xrpc.test.AbstractRPCTest;

public class TestZmqRPCPair extends AbstractRPCTest {

    private XrpcSender sender;
    private XrpcReceiver receiver;

    @Before
    public void setup() {
        String address = "localhost:8989";
        sender = getRpcBuilder().fixedSender()//
                .endpoint("zmq:pair:tcp:connect://" + address) //
                .build();

        receiver = getRpcBuilder().fixedReceiver() //
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
        this.receiver.subscribe(this::echo);

        var origin = BValue.of("this is test text");
        var response = this.sender.call(origin).get().body();

        assertEquals(origin, response);
    }
}
