package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ProducerInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Gauge;
import lombok.Getter;

public class PrometheusGaugeProducerInstrumenter implements ProducerInstrumenter {

    @Getter
    private Gauge gauge;

    public PrometheusGaugeProducerInstrumenter(String name, String help) {
        this.gauge = Gauge.build(name, help).register();
    }

    public PrometheusGaugeProducerInstrumenter(Gauge gauge) {
        this.gauge = gauge;
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg,
            Function<Message, Promise<Message, Exception>> supplier, String source) {
        gauge.inc();
        return supplier.apply(msg) //
                       .always((s, r, e) -> gauge.dec());
    }

}
