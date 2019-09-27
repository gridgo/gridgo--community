package io.gridgo.socket.nanomsg.test;

import static org.junit.Assert.assertEquals;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;

public class NNPushWithoutConnection {

    private static final int port = 8080;

    private static final String host = "localhost";
    private static final String address = host + ":" + port;
    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private int doAckSend(Producer producer, int numMessages) throws InterruptedException, PromiseException {
        for (int i = 0; i < numMessages; i++) {
            try {
                producer.sendWithAck(Message.of(Payload.of(BObject.ofSequence("index", i)))).get();
            } catch (Exception e) {
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

        var queryString = "batchingEnabled=false&maxBatchSize=2000&ringBufferSize=2048&sendTimeOut=0";

        var connector = RESOLVER.resolve("nanomsg:push:tcp://" + address + "?" + queryString);
        try {
            connector.start();
            int numMessages = (int) 1e6;
            int failedAt = doAckSend(connector.getProducer().orElseThrow(), numMessages);
            assertEquals(0, failedAt);
        } finally {
            connector.stop();
        }
    }
}
