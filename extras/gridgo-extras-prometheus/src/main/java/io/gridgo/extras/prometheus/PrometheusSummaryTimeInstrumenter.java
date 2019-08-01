package io.gridgo.extras.prometheus;

import java.util.function.Function;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.Promise;

import io.gridgo.framework.execution.ExecutionStrategyInstrumenter;
import io.gridgo.framework.support.Message;
import io.prometheus.client.Summary;
import lombok.Getter;

public class PrometheusSummaryTimeInstrumenter implements ExecutionStrategyInstrumenter {

    @Getter
    private Summary summary;

    public PrometheusSummaryTimeInstrumenter(String name, String help) {
        this.summary = Summary.build(name, help).register();
    }

    public PrometheusSummaryTimeInstrumenter(Summary summary) {
        this.summary = summary;
    }

    @Override
    public Runnable instrument(Message msg, Deferred<Message, Exception> deferred, Runnable runnable) {
        return () -> {
            var timer = summary.startTimer();
            deferred.promise() //
                    .always((s, r, e) -> timer.close());
            runnable.run();
        };
    }

    @Override
    public Promise<Message, Exception> instrument(Message msg, Function<Message, Promise<Message, Exception>> supplier,
            String source) {
        var timer = summary.startTimer();
        return supplier.apply(msg) //
                       .always((s, r, e) -> timer.close());
    }
}
