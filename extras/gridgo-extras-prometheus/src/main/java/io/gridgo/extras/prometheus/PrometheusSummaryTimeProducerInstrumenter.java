package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ProducerInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Summary;
import lombok.Getter;

public class PrometheusSummaryTimeProducerInstrumenter implements ProducerInstrumenter {

    @Getter
    private Summary summary;

    public PrometheusSummaryTimeProducerInstrumenter(String name, String help) {
        this.summary = Summary.build(name, help).register();
    }

    public PrometheusSummaryTimeProducerInstrumenter(Summary summary) {
        this.summary = summary;
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg, Function<Message, Promise<Message, Exception>> supplier,
            String source) {
        var timer = summary.startTimer();
        return supplier.apply(msg) //
                       .always((s, r, e) -> timer.close());
    }
}
