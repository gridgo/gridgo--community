package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ExecutionStrategyInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Histogram;
import lombok.Getter;

public class PrometheusHistorgramTimeInstrumenter implements ExecutionStrategyInstrumenter {

    @Getter
    private Histogram histogram;

    public PrometheusHistorgramTimeInstrumenter(String name, String help) {
        this.histogram = Histogram.build(name, help).register();
    }

    public PrometheusHistorgramTimeInstrumenter(String name, String help, double... buckets) {
        this.histogram = Histogram.build(name, help).buckets(buckets).register();
    }

    public PrometheusHistorgramTimeInstrumenter(Histogram histogram) {
        this.histogram = histogram;
    }

    @Override
    public Runnable instrument(Message msg, Deferred<Message, Exception> deferred, Runnable runnable) {
        return () -> {
            var timer = histogram.startTimer();
            deferred.promise() //
                    .always((s, r, e) -> timer.close());
            runnable.run();
        };
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg,
            Function<Message, Promise<Message, Exception>> supplier, String source) {
        var timer = histogram.startTimer();
        return supplier.apply(msg) //
                       .always((s, r, e) -> timer.close());
    }
}
