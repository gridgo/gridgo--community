package io.gridgo.xrpc.test.self;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;
import io.gridgo.xrpc.test.AbstractRPCTest;

public class TestHttpRPCWithDefaultHttpClient extends AbstractRPCTest {

    private XrpcSender sender;
    private XrpcReceiver receiver;

    @Before
    public void setup() {
        String address = "localhost:8989";
        sender = getRpcBuilder().selfSender() //
                .endpoint("http://" + address + "?method=POST") //
                .build();

        receiver = getRpcBuilder().selfReceiver() //
                .endpoint("jetty:http://" + address) //
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
        var response = sender.call(Message.ofAny(origin)).get();

        assertEquals(origin, response.body());
    }
}
