package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ProducerInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Counter;
import lombok.Getter;

public class PrometheusErrorCounterProducerInstrumenter implements ProducerInstrumenter {

    @Getter
    private Counter counter;

    public PrometheusErrorCounterProducerInstrumenter(String name, String help) {
        this.counter = Counter.build(name, help).register();
    }

    public PrometheusErrorCounterProducerInstrumenter(Counter counter) {
        this.counter = counter;
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg, Function<Message, Promise<Message, Exception>> supplier,
            String source) {
        return supplier.apply(msg).fail(e -> counter.inc());
    }
}
