package io.gridgo.rpc.fixed;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.bean.BValue;
import io.gridgo.rpc.AbstractRPCTest;
import io.gridgo.rpc.RpcReceiver;
import io.gridgo.rpc.RpcSender;

public class TestMixSendHttpReceiveZmq extends AbstractRPCTest {

    private RpcSender sender;
    private RpcReceiver receiver;

    @Before
    public void setup() {
        String address = "localhost:8989";
        String replyAddress = "localhost:8888";
        sender = getRpcBuilder().dynamicSender() //
                .endpoint("http2://" + address + "?method=POST") //
                .replyTo("zmq:push:tcp://" + replyAddress) //
                .replyEndpoint("zmq:pull:tcp://" + replyAddress) //
                .build();

        receiver = getRpcBuilder().dynamicReceiver()//
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
        var response = this.sender.send(origin).get();

        assertEquals(origin, response);
    }
}
