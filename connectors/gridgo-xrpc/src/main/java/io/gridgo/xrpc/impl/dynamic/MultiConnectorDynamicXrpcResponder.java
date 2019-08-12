package io.gridgo.xrpc.impl.dynamic;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.cliffc.high_scale_lib.NonBlockingHashMap;

import io.gridgo.connector.Connector;
import io.gridgo.connector.Producer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class MultiConnectorDynamicXrpcResponder<KeyType> extends AbstractDynamicXrpcResponder<KeyType> {

    private final List<Connector> connectors = new LinkedList<>();

    private final Map<KeyType, Producer> responders = new NonBlockingHashMap<>();

    @Override
    protected void onStop() {
        super.onStop();
        connectors.forEach(connector -> {
            try {
                connector.stop();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Stop connector error: " + connector.getName(), e);
            }
        });
    }

    protected final Producer buildResponder(String replyTo) {
        KeyType key = genKey(replyTo);
        if (key == null) {
            return getFixedResponder();
        }

        Producer responder = lookupResponder(key);
        if (responder != null)
            return responder;

        synchronized (responders) {
            responder = lookupResponder(key);
            if (responder != null)
                return responder;

            Connector connector = resolveConnector(replyTo);
            if (connector == null)
                throw new RuntimeException("Connector cannot be resolved from reply endpoint: " + replyTo);

            connector.start();

            Optional<Producer> producerOpt = connector.getProducer();

            if (producerOpt.isEmpty()) {
                connector.stop();
                throw new RuntimeException("Producer is not available for endpoint: " + replyTo);
            }

            connectors.add(connector);

            responder = producerOpt.get();
            responders.put(key, responder);

            onResponderReady(replyTo, key, responder);

            return responder;
        }
    }

    protected void onResponderReady(String replyTo, KeyType refKey, Producer responder) {

    }

    protected abstract KeyType genKey(String replyTo);

    protected final Producer lookupResponder(KeyType key) {
        if (key == null)
            return null;
        return responders.get(key);
    }
}
