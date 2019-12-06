package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertTrue;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.bean.BObject;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.resolvers.ClasspathConnectorResolver;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ZMQPushPullUnitTest {

    private final ConnectorResolver RESOLVER = new ClasspathConnectorResolver("io.gridgo.connector");

    private void doAckSend(Consumer consumer, Producer producer) throws InterruptedException, PromiseException {
        int numMessages = (int) 1e2;
        CountDownLatch doneSignal = new CountDownLatch(numMessages);

        consumer.subscribe((message) -> {
            doneSignal.countDown();
        });

        long start = System.nanoTime();
        for (int i = 0; i < numMessages; i++) {
            producer.sendWithAck(Message.of(Payload.of(BObject.ofSequence("index", i)))).get();
        }

        if (doneSignal.await(1, TimeUnit.MINUTES)) {
            double elapsed = Double.valueOf(System.nanoTime() - start);
            DecimalFormat df = new DecimalFormat("###,###.##");
            log.debug("ACK TRANSMITION DONE, {} messages were transmited in {} ms -> pace: {} msg/s", numMessages,
                    df.format(elapsed / 1e6), df.format(1e9 * numMessages / elapsed));

            consumer.clearSubscribers();
        } else {
            consumer.clearSubscribers();
            throw new RuntimeException("Test cannot be done after 1 min");
        }

    }

    private void doFnFSend(Consumer consumer, Producer producer) throws InterruptedException {
        int numMessages = (int) 1e3;
        CountDownLatch doneSignal = new CountDownLatch(numMessages);

        consumer.subscribe((message) -> {
            doneSignal.countDown();
        });
        long start = System.nanoTime();
        for (int i = 0; i < numMessages; i++) {
            producer.send(Message.of(Payload.of(BObject.ofSequence("index", i))));
        }

        if (doneSignal.await(1, TimeUnit.MINUTES)) {
            double elapsed = Double.valueOf(System.nanoTime() - start);
            DecimalFormat df = new DecimalFormat("###,###.##");
            log.debug("FnF TRANSMITION DONE, {} messages were transmited in {} ms -> pace: {} msg/s",
                    df.format(elapsed / 1e6), df.format(1e9 * numMessages / elapsed), numMessages);
            consumer.clearSubscribers();
        } else {
            consumer.clearSubscribers();
            throw new RuntimeException("Test cannot be done after 1 min");
        }

    }

    @Test
    public void testMonoplex() throws InterruptedException, PromiseException {

        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;

        var queryString = "batchingEnabled=true&maxBatchSize=2000&ringBufferSize=2048";
        var pairs = new Pair[] { Pair.of("tcp", "localhost:8888"), Pair.of("ipc", "test.sock") };

        for (var pair : pairs) {
            log.debug("Test ZMQ push/pull with: {}", pair);
            var protocol = pair.getLeft();
            var address = pair.getRight();

            var connector1 = RESOLVER.resolve("zmq:pull:" + protocol + "://" + address);
            var connector2 = RESOLVER.resolve("zmq:push:" + protocol + "://" + address + "?" + queryString);

            try {
                connector1.start();
                assertTrue(connector1.getConsumer().isPresent());
                var consumer = connector1.getConsumer().get();

                connector2.start();
                assertTrue(connector2.getProducer().isPresent());
                var producer = connector2.getProducer().get();

                warmUp(consumer, producer);

                this.doFnFSend(consumer, producer);
                this.doAckSend(consumer, producer);
            } finally {
                connector1.stop();
                connector2.stop();
            }
        }
    }

    private void warmUp(Consumer consumer, Producer producer) throws PromiseException, InterruptedException {
        var doneSignal = new CountDownLatch(1);
        consumer.subscribe((msg) -> doneSignal.countDown());
        producer.send(Message.of(Payload.of(BObject.ofSequence("cmd", "start"))));
        doneSignal.await();

        log.debug("Warmup done");
        consumer.clearSubscribers();
    }
}
