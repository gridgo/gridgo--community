package io.gridgo.socket.zmq.test;

import static org.junit.Assert.assertTrue;

import org.joo.promise4j.PromiseException;
import org.junit.Test;

import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorFactory;
import io.gridgo.connector.Consumer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.socket.HasBindingPort;

public class ZMQRandomPortBinding {

    private static final String address = "localhost";
    private final static ConnectorFactory connectorFactory = new DefaultConnectorFactory();

    @Test
    public void testPairBind() throws InterruptedException, PromiseException {

        String osName = System.getProperty("os.name");
        if (osName != null && osName.contains("Windows"))
            return;

        Connector connector = connectorFactory.createConnector("zmq:pair:tcp:bind://" + address);

        connector.start();
        assertTrue(connector.getConsumer().isPresent());

        Consumer consumer = connector.getConsumer().get();

        assertTrue(consumer instanceof HasBindingPort);
        int bindingPort = ((HasBindingPort) consumer).getBindingPort();
        assertTrue(bindingPort > 0);

        System.out.println("Binding to random port: " + bindingPort);

        connector.stop();
    }
}
