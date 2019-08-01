package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ProducerInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Histogram;
import lombok.Getter;

public class PrometheusHistorgramTimeProducerInstrumenter implements ProducerInstrumenter {

    @Getter
    private Histogram histogram;

    public PrometheusHistorgramTimeProducerInstrumenter(String name, String help) {
        this.histogram = Histogram.build(name, help).register();
    }

    public PrometheusHistorgramTimeProducerInstrumenter(String name, String help, double... buckets) {
        this.histogram = Histogram.build(name, help).buckets(buckets).register();
    }

    public PrometheusHistorgramTimeProducerInstrumenter(Histogram histogram) {
        this.histogram = histogram;
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg,
            Function<Message, Promise<Message, Exception>> supplier, String source) {
        var timer = histogram.startTimer();
        return supplier.apply(msg) //
                       .always((s, r, e) -> timer.close());
    }
}
