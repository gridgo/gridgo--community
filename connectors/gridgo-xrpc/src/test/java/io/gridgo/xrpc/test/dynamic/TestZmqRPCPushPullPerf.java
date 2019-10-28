package io.gridgo.xrpc.test.dynamic;

import static io.gridgo.utils.ThreadUtils.isShuttingDown;
import static io.gridgo.utils.ThreadUtils.registerShutdownTask;

import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import org.joo.promise4j.PromiseException;

import io.gridgo.bean.BValue;
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
        var sender = getRpcBuilder().dynamicSender() //
                .endpoint("zmq:push:tcp://" + address) //
                .replyTo("zmq:push:tcp://" + replyAddress) //
                .replyEndpoint("zmq:pull:tcp://" + replyAddress) //
                .build();

        var receiver = getRpcBuilder().dynamicReceiver()//
                .endpoint("zmq:pull:tcp://" + address) //
                .build();

        sender.start();
        receiver.start();

        registerShutdownTask(sender::stop);
        registerShutdownTask(receiver::stop);

        new TestZmqRPCPushPullPerf(sender, receiver).test();

        ThreadUtils.sleep(500);
        System.exit(0);
    }

    private void test() throws PromiseException, InterruptedException {
        receiver.subscribe((msg, defferred) -> defferred.resolve(msg));
        var request = BValue.of("This is test text");

        var sent = new AtomicLong(0);
        var doneCount = new AtomicLong();

        var monitor = new Thread(() -> {
            long last = 0;
            var formatter = new DecimalFormat("###,###.##");
            while (!Thread.currentThread().isInterrupted()) {
                if (!ThreadUtils.sleepSilence(1000))
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

        while (true) {
            sender.call(request).always((stt, resp, ex) -> doneCount.incrementAndGet());
            sent.incrementAndGet();
            if (isShuttingDown())
                break;
        }
    }
}
