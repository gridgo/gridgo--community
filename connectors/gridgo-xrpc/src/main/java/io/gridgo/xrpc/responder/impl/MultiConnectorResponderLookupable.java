package io.gridgo.xrpc.responder.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.xrpc.impl.AbstractConnectorResolvable;
import io.gridgo.xrpc.responder.XrpcResponder;
import io.gridgo.xrpc.responder.XrpcResponderLookupable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MultiConnectorResponderLookupable extends AbstractConnectorResolvable implements XrpcResponderLookupable {

    private final List<Connector> connectors = new LinkedList<>();

    private final Map<String, XrpcResponder> responders = new NonBlockingHashMap<>();

    public MultiConnectorResponderLookupable(ConnectorResolver resolver) {
        this.setConnectorResolver(resolver);
    }

    @Override
    protected void onStop() {
        super.onStop();
        connectors.forEach(connector -> {
            try {
                connector.stop();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Stop connector error: {}", connector.getName(), e);
            }
        });
    }

    private XrpcResponder buildResponder(@NonNull String replyTo) {
        synchronized (responders) {
            var responder = responders.get(replyTo);
            if (responder != null)
                return responder;

            Connector connector = resolveConnector(replyTo);
            if (connector == null)
                throw new RuntimeException("Connector cannot be resolved from reply endpoint: " + replyTo);

            connector.start();

            var producerOpt = connector.getProducer();

            if (producerOpt.isEmpty()) {
                connector.stop();
                throw new RuntimeException("Producer is not available for endpoint: " + replyTo);
            }

            connectors.add(connector);

            responder = new FixedXrpcResponder(producerOpt.get());
            responders.put(replyTo, responder);

            return responder;
        }
    }

    @Override
    public final XrpcResponder lookup(String replyTo) {
        if (replyTo == null)
            return null;
        var responder = responders.get(replyTo);
        if (responder != null)
            return responder;

        return this.buildResponder(replyTo);
    }
}
