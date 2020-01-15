package io.gridgo.extras.config;

import org.joo.promise4j.Promise;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorFactory;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.Producer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.extras.config.exceptions.NoProducerException;
import io.gridgo.framework.support.Message;

public class ProducerConfigurator extends AbstractConnectorConfigurator {

    private Message msg;

    private ProducerConfigurator(Connector connector, boolean owned, Message msg) {
        super(connector, owned);
        this.msg = msg;
    }

    public static final ProducerConfigurator ofConnector(Connector connector, Message msg) {
        return new ProducerConfigurator(connector, false, msg);
    }

    public static final ProducerConfigurator ofEndpoint(String endpoint, Message msg) {
        var connector = new DefaultConnectorFactory().createConnector(endpoint);
        return new ProducerConfigurator(connector, true, msg);
    }

    public static final ProducerConfigurator ofEndpoint(String endpoint, Message msg, ConnectorFactory factory) {
        var connector = factory.createConnector(endpoint);
        return new ProducerConfigurator(connector, true, msg);
    }

    public static final ProducerConfigurator ofEndpoint(String endpoint, Message msg, ConnectorResolver resolver) {
        var connector = resolver.resolve(endpoint);
        return new ProducerConfigurator(connector, true, msg);
    }

    public static final ProducerConfigurator ofEndpoint(String endpoint, Message msg, ConnectorResolver resolver,
            ConnectorContext context) {
        var connector = resolver.resolve(endpoint, context);
        return new ProducerConfigurator(connector, true, msg);
    }

    @Override
    protected void onStart() {
        super.onStart();
        getConnector().getProducer() //
                      .ifPresentOrElse(this::resolveWithProducer, this::onNoProducer);
    }

    private Promise<BElement, Exception> resolveWithProducer(Producer producer) {
        return producer.call(msg) //
                       .filterDone(Message::body) //
                       .done(this::publishLoaded) //
                       .fail(this::publishFailed);
    }

    private void onNoProducer() {
        publishFailed(new NoProducerException("No producer available for connector " + getConnector().getName()));
    }

    @Override
    protected String generateName() {
        return "config.producer." + getConnector().getName();
    }
}
