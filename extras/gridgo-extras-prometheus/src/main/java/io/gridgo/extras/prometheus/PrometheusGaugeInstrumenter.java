package io.gridgo.extras.prometheus;

import org.joo.promise4j.Deferred;

import io.gridgo.framework.execution.ExecutionStrategyInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Gauge;
import lombok.Getter;

public class PrometheusGaugeInstrumenter implements ExecutionStrategyInstrumenter {

    @Getter
    private Gauge gauge;

    public PrometheusGaugeInstrumenter(String name, String help) {
        this.gauge = Gauge.build(name, help).register();
    }

    public PrometheusGaugeInstrumenter(Gauge gauge) {
        this.gauge = gauge;
    }

    @Override
    public Runnable instrument(Message msg, Deferred<Message, Exception> deferred, Runnable runnable) {
        return () -> {
            gauge.inc();
            runnable.run();
            deferred.promise() //
                    .always((s, r, e) -> gauge.dec());
        };
    }
}
