package io.gridgo.xrpc.test.dynamic;

import static io.gridgo.utils.ThreadUtils.isShuttingDown;
import static io.gridgo.utils.ThreadUtils.registerShutdownTask;
import static io.gridgo.utils.ThreadUtils.sleepSilence;
import static java.lang.Thread.currentThread;

import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.PromiseException;

import com.lmax.disruptor.WorkHandler;
import com.lmax.disruptor.dsl.Disruptor;

import io.gridgo.bean.BValue;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.ThreadUtils;
import io.gridgo.xrpc.XrpcReceiver;
import io.gridgo.xrpc.XrpcSender;
import io.gridgo.xrpc.test.AbstractRPCTest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class TestZmqRPCPushPullPerf extends AbstractRPCTest {

    private XrpcSender sender;
    private XrpcReceiver receiver;

    public static void main(String[] args) throws Exception {
        String address = "localhost:8989";
        String replyAddress = "localhost:8888";

        var defaultParams = "?batchingEnabled=true&maxBatchSize=1000&sndhwm=100000&bufferSize=4194304&monitorEnabled=false";
        var sender = getRpcBuilder().dynamicSender() //
                .endpoint("zmq:push:tcp://" + address + defaultParams) //
                .replyTo("zmq:push:tcp://" + replyAddress + defaultParams) //
                .replyEndpoint("zmq:pull:tcp://" + replyAddress + "?bufferSize=4194304&monitorEnabled=false") //
                .build();

        var receiver = getRpcBuilder().dynamicReceiver()//
                .endpoint("zmq:pull:tcp://" + address + "?bufferSize=4194304&monitorEnabled=true") //
                .build();

        sender.start();
        receiver.start();

        registerShutdownTask(sender::stop);
        registerShutdownTask(receiver::stop);

        new TestZmqRPCPushPullPerf(sender, receiver).test();

        ThreadUtils.sleep(500);
        System.exit(0);
    }

    private static class InternalEvent {
        Message msg;
        Deferred<Message, Exception> deferred;
    }

    private void test() throws PromiseException, InterruptedException {
        var disruptor = initDisruptor();
        receiver.subscribe((msg, deferred) -> disruptor.publishEvent((event, sequence) -> {
            event.deferred = deferred;
            event.msg = msg;
        }));

        var request = BValue.of("This is test text");

        var sent = new AtomicLong(0);
        var doneCount = new AtomicLong();
        var doneSignal = new CountDownLatch(1);

        var monitor = new Thread(() -> {
            long last = 0;
            var formatter = new DecimalFormat("###,###.##");
            while (!currentThread().isInterrupted()) {
                if (!sleepSilence(1000))
                    return;

                long done = doneCount.get();
                double pace = done - last;
                last = done;
                log.debug("sent: {}, done: {} ({}%) -> pace: {} tps", //
                        formatter.format(sent.get()), //
                        formatter.format(done), //
                        formatter.format(done * 100.0 / sent.get()), //
                        formatter.format(pace));
            }
        }, "monitor");
        monitor.start();

        int numMessages = (int) 1e6;

        for (int i = 0; i < numMessages; i++) {
            sender.call(request).always((stt, resp, ex) -> {
                if (ex != null)
                    ex.printStackTrace();

                if (doneCount.incrementAndGet() == numMessages)
                    doneSignal.countDown();
            });

            sent.incrementAndGet();
            if (isShuttingDown())
                break;
        }

        doneSignal.await();
        ThreadUtils.sleep(500);
        monitor.interrupt();
    }

    @SuppressWarnings("unchecked")
    private Disruptor<InternalEvent> initDisruptor() {
        var namePattern = "Test XRPC worker #%d";
        var seed = new AtomicInteger();
        var threadFactory = (ThreadFactory) r -> new Thread(r, String.format(namePattern, seed.incrementAndGet()));
        var disruptor = new Disruptor<InternalEvent>(InternalEvent::new, 4096, threadFactory);
        WorkHandler<InternalEvent>[] workers = new WorkHandler[4];
        for (int i = 0; i < workers.length; i++)
            workers[i] = event -> echo(event.msg, event.deferred);

        disruptor.handleEventsWithWorkerPool(workers);
        disruptor.start();
        registerShutdownTask(disruptor::shutdown);
        return disruptor;
    }
}
