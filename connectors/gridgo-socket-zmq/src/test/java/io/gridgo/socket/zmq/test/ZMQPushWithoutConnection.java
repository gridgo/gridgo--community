package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class ZMQPushWithoutConnection {

    private static final int port = 8080;

    private static final String host = "localhost";
    private static final String address = host + ":" + port;
    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private int doAckSend(Producer producer, int numMessages) throws InterruptedException, PromiseException {
        for (int i = 0; i < numMessages; i++) {
            long startTime = System.currentTimeMillis();
            try {
                producer.sendWithAck(Message.of(Payload.of(BObject.ofSequence("index", i)))).get();
            } catch (Exception e) {
                long errorAfter = System.currentTimeMillis() - startTime;
                System.err.println("Queued/sent " + i + " msg, error after " + errorAfter + "ms");
                return i;
            }
        }
        return numMessages;
    }

    @Test
    public void testMonoplex() throws InterruptedException, PromiseException {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;

        int hwm = 10;
        var queryString = "batchingEnabled=false&maxBatchSize=2000&ringBufferSize=2048&sndhwm=" + hwm
                + "&sendTimeOut=2000";

        var connector = RESOLVER.resolve("zmq:push:tcp://" + address + "?" + queryString);
        try {
            connector.start();
            int numMessages = (int) 1e6;
            int failedAt = doAckSend(connector.getProducer().orElseThrow(), numMessages);
            assertEquals(hwm, failedAt);
        } finally {
            connector.stop();
        }
    }
}
