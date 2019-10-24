package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ProducerInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Counter;
import lombok.Getter;

public class PrometheusCounterProducerInstrumenter implements ProducerInstrumenter {

    @Getter
    private Counter counter;

    public PrometheusCounterProducerInstrumenter(String name, String help) {
        this.counter = Counter.build().name(name).help(help).register();
    }

    public PrometheusCounterProducerInstrumenter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg, Function<Message, Promise<Message, Exception>> supplier,
            String source) {
        counter.inc();
        return supplier.apply(msg);
    }
}
