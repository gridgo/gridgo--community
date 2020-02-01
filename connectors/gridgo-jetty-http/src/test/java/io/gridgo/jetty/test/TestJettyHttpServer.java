package io.gridgo.jetty.test;

import static io.gridgo.connector.httpcommon.HttpCommonConstants.URI_TEMPLATE_VARIABLES;
import static java.net.http.HttpRequest.newBuilder;
import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.util.Strings;
import org.junit.Test;

import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.connector.jetty.server.JettyRequestHandler;
import io.prometheus.client.CollectorRegistry;

public class TestJettyHttpServer {

    private static final String TEST_TEXT = "this is test text";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final JettyHttpServerManager serverManager = JettyHttpServerManager.getInstance();

    private static final AtomicInteger PORT_SEED = new AtomicInteger(8000);

    private static String genAddress(String host) {
        return host + ":" + PORT_SEED.getAndIncrement();
    }

    @Test
    public void testAllInterfaceThenLocalhost() {
        var httpServer1 = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
        var httpServer2 = serverManager.getOrCreateJettyServer("localhost:8889", true);
        var httpServer3 = serverManager.getOrCreateJettyServer("*:8889", true);

        assertEquals(httpServer1, httpServer2);
        assertEquals(httpServer1, httpServer3);

        // start then stop to remove current http server from manager, make sure other
        // test case run properly
        httpServer1.start();
        httpServer1.stop();
    }

    @Test
    public void testLocalhostThenAllInterface() throws URISyntaxException, IOException, InterruptedException {
        var osName = System.getProperty("os.name");
        var localhostReceived = new AtomicReference<String>(null);
        var localhostServer = serverManager.getOrCreateJettyServer("localhost:8889", true);

        localhostServer.start();
        localhostServer.addPathHandler("/*", (req, res) -> localhostReceived.set(req.getParameter("key")));

        if (osName.equalsIgnoreCase("Mac OS X")) {
            var allInterfaceServer = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
            assertNotEquals(localhostServer, allInterfaceServer);
            allInterfaceServer.start();

            var allInterfaceReceived = new AtomicReference<String>(null);
            allInterfaceServer.addPathHandler("/*", (req, res) -> allInterfaceReceived.set(req.getParameter("key")));

            final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
            URI uri = new URI("http://localhost:8889/?key=" + encodedText);
            HttpRequest request = newBuilder().GET().uri(uri).build();
            httpClient.send(request, ofString()).body();

            assertNull(allInterfaceReceived.get());
            assertEquals(TEST_TEXT, localhostReceived.get());

            allInterfaceServer.stop();
        }

        localhostServer.stop();
    }

    private JettyRequestHandler fixedResponse(String response, AtomicReference<Throwable> exceptionHolder) {
        return (req, res) -> {
            try (var writer = res.getWriter()) {
                writer.write(response);
            } catch (Exception e) {
                exceptionHolder.set(e);
            }
        };
    }

    @Test
    public void testMultiHandler() throws IOException, InterruptedException, URISyntaxException {
        var address = genAddress("localhost");
        JettyHttpServer httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        var exceptionHolder = new AtomicReference<Throwable>(null);

        httpServer.addPathHandler("/path1/*", fixedResponse("path1", exceptionHolder)) //
                .addPathHandler("/path2/*", fixedResponse("path2", exceptionHolder)) //
                .addPathHandler("/*", fixedResponse("all", exceptionHolder));

        final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());

        var uri = new URI("http://" + address + "/path1/subpath?key=" + encodedText);
        var request = newBuilder(uri).build();
        var resp = httpClient.send(request, ofString()).body();
        assertEquals("path1", resp);

        uri = new URI("http://" + address + "/path2/subpath?key=" + encodedText);
        request = newBuilder(uri).build();
        resp = httpClient.send(request, ofString()).body();
        assertEquals("path2", resp);

        uri = new URI("http://" + address + "/other-path?key=" + encodedText);
        request = newBuilder(uri).build();
        resp = httpClient.send(request, ofString()).body();
        assertEquals("all", resp);

        assertNull(exceptionHolder.get());

        httpServer.stop();
    }

    @Test
    public void testPingPong() throws IOException, InterruptedException, URISyntaxException {
        var address = genAddress("localhost");
        var httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        final var exception = new AtomicReference<Throwable>(null);

        httpServer.addPathHandler("/*", this::echo);

        String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
        URI uri = new URI("http://" + address + "/?key=" + encodedText);
        HttpRequest request = newBuilder().GET().uri(uri).build();
        HttpClient httpClient = HttpClient.newHttpClient();
        var resp = httpClient.send(request, ofString()).body();

        assertNull(exception.get());
        assertEquals(TEST_TEXT, resp);

        httpServer.stop();
    }

    @SuppressWarnings("unchecked")
    private void echo(HttpServletRequest req, HttpServletResponse res) {
        try (var writer = res.getWriter()) {
            var key = req.getParameter("key");
            if (key == null)
                key = ((Map<String, String>) req.getAttribute(URI_TEMPLATE_VARIABLES)).get("key");
            writer.write(key == null ? Strings.EMPTY : key);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Double getSampleValue(String name) {
        return CollectorRegistry.defaultRegistry.getSampleValue(name);
    }

    private Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
        return CollectorRegistry.defaultRegistry.getSampleValue(name, labelNames, labelValues);
    }

    @Test
    public void testPrometheus() throws Exception {
        var path = "/prometheus";
        var address = genAddress("127.0.0.1");
        var prometheusPrefix = "myCustomPrefix";

        var httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.addPathHandler(path, this::echo, true, prometheusPrefix).start();

        var httpClient = HttpClient.newHttpClient();

        var encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
        var resp = httpClient.send( //
                newBuilder(new URI("http://" + address + path + "?key=" + encodedText)).build(), //
                ofString());

        // make sure the call got correct response
        assertThat(resp.body(), is(TEST_TEXT));

        // add another path handler which disable prometheus
        var path1 = "/non-prometheus";
        httpServer.addPathHandler(path1 + "/{key}", this::echo);

        // request that second endpoint
        resp = httpClient.send( //
                newBuilder(new URI("http://" + address + path1 + "/" + encodedText)).build(), //
                ofString());

        // make sure the second endpoint work
        assertThat(resp.body(), is(encodedText));

        // make sure only endpoint 1 has statistics
        assertThat(getSampleValue(prometheusPrefix + "_requests_total"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_requests_active"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_requests_active_max"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_request_time_max"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_request_time_total"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_total"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_active"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_active_max"), is(greaterThan(0.0)));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_time_max"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_time_total"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_async_requests_total"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_async_requests_waiting"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_async_requests_waiting_max"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_async_dispatches_total"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_expires_total"), is(0.0));

        var labelNames = new String[] { "code" };
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "1xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "2xx" }), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "3xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "4xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "5xx" }), is(0.0));

        assertThat(getSampleValue(prometheusPrefix + "_stats_seconds"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_responses_bytes_total"), is(notNullValue()));

        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "1xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "2xx" }), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "3xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "4xx" }), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_responses_total", labelNames, new String[] { "5xx" }), is(0.0));

        httpServer.stop();
    }
}
