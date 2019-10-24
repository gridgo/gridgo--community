package io.gridgo.socket.zmq.test;

import static io.gridgo.connector.impl.factories.DefaultConnectorFactory.DEFAULT_CONNECTOR_RESOLVER;
import static io.gridgo.utils.ThreadUtils.sleep;
import static java.lang.Math.min;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.joo.promise4j.PromiseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;

public class ZMQPushBeforePullTest {

    private static final String ADDRESS = "localhost:8089";

    private int hwm = 10;
    private Connector connector1;
    private Connector connector2;

    private int doAckSend(Producer producer, int numMessages) throws InterruptedException, PromiseException {
        for (int i = 0; i < numMessages; i++) {
            try {
                producer.sendWithAck(Message.ofAny(i)).get();
            } catch (Exception e) {
                return i;
            }
        }
        return numMessages;
    }

    private boolean okToContinue() {
        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return false;
        return true;
    }

    @Before
    public void setUp() {
        if (!okToContinue())
            return;

        var params = "?batchingEnabled=false&maxBatchSize=2000&ringBufferSize=2048&sendtimeout=0&sndhwm=" + hwm;

        connector1 = DEFAULT_CONNECTOR_RESOLVER.resolve("zmq:push:tcp://" + ADDRESS + params);
        connector2 = DEFAULT_CONNECTOR_RESOLVER.resolve("zmq:pull:tcp://" + ADDRESS);
    }

    @After
    public void tearDown() {
        if (!okToContinue())
            return;

        connector1.stop();
        connector2.stop();
    }

    @Test
    public void testPushBeforePull() throws InterruptedException, PromiseException {
        if (!okToContinue())
            return;

        connector1.start();
        int numQueuedMsgs = doAckSend(connector1.getProducer().orElseThrow(), 100);
        
        var counter = new AtomicInteger(0);
        connector2.getConsumer().orElseThrow().subscribe(msg -> counter.incrementAndGet());
        connector2.start();
        sleep(200);

        assertEquals(min(numQueuedMsgs, hwm), counter.get());
    }
}
