package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ExecutionStrategyInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Counter;
import lombok.Getter;

public class PrometheusCounterInstrumenter implements ExecutionStrategyInstrumenter {

    @Getter
    private Counter counter;

    public PrometheusCounterInstrumenter(String name, String help) {
        this.counter = Counter.build().name(name).help(help).register();
    }

    public PrometheusCounterInstrumenter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public Runnable instrument(Message msg, Deferred<Message, Exception> deferred, Runnable runnable) {
        return () -> {
            counter.inc();
            runnable.run();
        };
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg, Function<Message, Promise<Message, Exception>> supplier,
            String source) {
        counter.inc();
        return supplier.apply(msg);
    }
}
