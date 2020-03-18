package io.gridgo.jetty.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;

import org.junit.Test;

import io.gridgo.bean.BElement;
import io.gridgo.connector.Connector;
import io.gridgo.connector.ConnectorResolver;
import io.gridgo.connector.httpcommon.HttpHeader;
import io.gridgo.connector.impl.factories.DefaultConnectorFactory;
import io.gridgo.connector.jetty.JettyConnector;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.framework.support.Message;

public class TestPathTemplate {

    private static final String SCHEME = "jetty";

    private static final String TEST_TEXT = "this is test text";

    private final String HTTP_LOCALHOST_8888 = "http://localhost:8888";

    private final ConnectorResolver resolver = DefaultConnectorFactory.DEFAULT_CONNECTOR_RESOLVER;
    private final HttpClient httpClient = HttpClient.newHttpClient();

    private Connector createConnector(String endpoint) {
        var connector = resolver.resolve(endpoint);
        assertNotNull(connector);
        assertTrue(connector instanceof JettyConnector);
        assertTrue(connector.getConsumer().isPresent());
        assertTrue(connector.getProducer().isPresent());
        assertTrue(connector.getConsumer().get() instanceof JettyConsumer);
        assertTrue(connector.getProducer().get() instanceof JettyResponder);
        return connector;
    }

    @Test
    public void testPingPongGET() throws URISyntaxException, IOException, InterruptedException {
        var path = "key";
        var baseUri = HTTP_LOCALHOST_8888 + "/" + path;
        var connectorEndpoint = SCHEME + ":" + HTTP_LOCALHOST_8888 + "/[" + path + "/:key]";
        // jetty:http://localhost:8888/key/:key; -> http://localhost:8888/key/abchg ->
        // query_param: { key: "abchg" }
        var connector = createConnector(connectorEndpoint);
        connector.start();

        try {
            var consumer = connector.getConsumer().get();

            consumer.subscribe((msg, deferred) -> {
                var queryParams = msg.headers().get(HttpHeader.QUERY_PARAMS.asString());
                deferred.resolve(Message.ofAny(queryParams));
            });

            var encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
            var uri = new URI(baseUri + "/" + encodedText);

            var request = HttpRequest.newBuilder().GET().uri(uri).build();
            var response = httpClient.send(request, BodyHandlers.ofString());

            var respObj = BElement.ofJson(response.body());
            System.out.println(respObj);
            assertNotNull(respObj);
            assertTrue(respObj.isObject());
            assertEquals(TEST_TEXT, respObj.asObject().getString("key"));
        } finally {
            connector.stop();
        }
    }
}
