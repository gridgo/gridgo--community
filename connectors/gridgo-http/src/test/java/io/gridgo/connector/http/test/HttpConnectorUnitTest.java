package io.gridgo.connector.http.test;

import org.asynchttpclient.Dsl;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import io.gridgo.connector.http.HttpConnector;
import io.gridgo.connector.http.HttpProducer;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.framework.support.impl.SimpleRegistry;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.resolver.dns.DnsNameResolverBuilder;

public class HttpConnectorUnitTest {

    @Test
    public void testHttp() throws InterruptedException {
        var url = "https://raw.githubusercontent.com/gridgo/gridgo-connector/dungba/developing/connectors/gridgo-http/src/test/resources/test.txt?nameResolverBean=nameResolver&format=string";
        var eventLoopGroup = new NioEventLoopGroup();
        var factory = new DefaultConnectorFactory();
        var nameResolver = new DnsNameResolverBuilder() //
                .channelType(NioDatagramChannel.class) //
                .eventLoop(eventLoopGroup.next()) //
                .queryTimeoutMillis(1000).build();
        factory.setRegistry(new SimpleRegistry().register("nameResolver", nameResolver));

        var connector = factory.createConnector(url);
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        producer.send(null);
        var latch = new CountDownLatch(2);
        var atomic = new AtomicReference<Exception>();
        producer.sendWithAck(null).always((status, response, ex) -> {
            if (ex != null)
                atomic.set(ex);
            latch.countDown();
        });
        producer.call(Message.of(Payload.of(null))).always((status, response, ex) -> {
            if (ex != null) {
                atomic.set(ex);
            } else {
                var body = response.body().asValue().getString();
                if (!"hello".equals(body)) {
                    atomic.set(new RuntimeException("expected 'hello', got '" + body + "'"));
                }
            }
            latch.countDown();
        });

        latch.await();

        if (atomic.get() != null)
            atomic.get().printStackTrace();

        Assert.assertNull(atomic.get());
    }

    @Test
    public void testHttpNoNameResolver() throws InterruptedException {
        var url = "https://raw.githubusercontent.com/gridgo/gridgo-connector/dungba/developing/connectors/gridgo-http/src/test/resources/test.txt?format=string";
        var factory = new DefaultConnectorFactory();

        var connector = factory.createConnector(url);
        var producer = connector.getProducer().orElseThrow();
        connector.start();

        producer.send(null);
        var latch = new CountDownLatch(2);
        var atomic = new AtomicReference<Exception>();
        producer.sendWithAck(null).always((status, response, ex) -> {
            if (ex != null)
                atomic.set(ex);
            latch.countDown();
        });
        producer.call(Message.ofEmpty()).always((status, response, ex) -> {
            if (ex != null) {
                atomic.set(ex);
            } else {
                var body = response.body().asValue().getString();
                if (!"hello".equals(body)) {
                    atomic.set(new RuntimeException("expected 'hello', got '" + body + "'"));
                }
            }
            latch.countDown();
        });

        latch.await();

        if (atomic.get() != null)
            atomic.get().printStackTrace();

        Assert.assertNull(atomic.get());
    }

    @Test
    public void testUri() {
        String[] testCases = new String[] { //
                "http://google.com", //
                "http://google.com:80", //
                "https://google.com", //
                "https://google.com/test" //
        };
        for (String test : testCases) {
            var connector = new DefaultConnectorFactory().createConnector(test);
            Assert.assertNotNull(connector);
            Assert.assertTrue(connector instanceof HttpConnector);
        }
    }

    @Test
    public void testSharedHttpClient() throws NoSuchFieldException, SecurityException, IllegalArgumentException,
            IllegalAccessException, IOException {
        var sharedHttpClient = Dsl.asyncHttpClient();

        var registry = new SimpleRegistry();
        registry.register("testHttpClient", sharedHttpClient);

        var connectorFactory = new DefaultConnectorFactory();
        connectorFactory.setRegistry(registry);

        var connector1 = connectorFactory.createConnector("http://www.google.com/test?sharedHttpClient=testHttpClient");
        var connector2 = connectorFactory.createConnector("https://www.google.com?sharedHttpClient=testHttpClient");

        connector1.start();
        connector2.start();

        assertTrue(connector1 instanceof HttpConnector);
        assertTrue(connector2 instanceof HttpConnector);

        assertTrue(connector1.getProducer().isPresent());
        assertTrue(connector2.getProducer().isPresent());

        var producer1 = connector1.getProducer().get();
        var producer2 = connector2.getProducer().get();

        var asyncHttpClientField = HttpProducer.class.getDeclaredField("asyncHttpClient");
        asyncHttpClientField.setAccessible(true);

        assertEquals(sharedHttpClient, asyncHttpClientField.get(producer1));
        assertEquals(sharedHttpClient, asyncHttpClientField.get(producer2));

        connector1.stop();
        connector2.stop();

        assertFalse(sharedHttpClient.isClosed());
        sharedHttpClient.close();
    }
}
