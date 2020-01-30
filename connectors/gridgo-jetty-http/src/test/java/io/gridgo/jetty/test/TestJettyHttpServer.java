package io.gridgo.jetty.test;

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
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Test;

import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.connector.jetty.server.JettyRequestHandler;
import io.gridgo.utils.ThreadUtils;
import io.prometheus.client.CollectorRegistry;

public class TestJettyHttpServer {

    private static final String TEST_TEXT = "this is test text";

    private static final HttpClient httpClient = HttpClient.newHttpClient();

    private final JettyHttpServerManager serverManager = JettyHttpServerManager.getInstance();
    private final String address = "localhost:8888";

    @Test
    public void testAllInterfaceThenLocalhost() {
        System.out.println("Test create server binding on all interface (0.0.0.0) then localhost");

        var httpServer1 = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
        var httpServer2 = serverManager.getOrCreateJettyServer("localhost:8889", true);
        var httpServer3 = serverManager.getOrCreateJettyServer("*:8889", true);

        assertEquals(httpServer1, httpServer2);
        assertEquals(httpServer1, httpServer3);

        // start then stop to remove current http server from manager, make sure other
        // test case run properly
        httpServer1.start();
        httpServer1.stop();

        System.out.println("*** DONE ***");
    }

    @Test
    public void testLocalhostThenAllInterface() throws URISyntaxException, IOException, InterruptedException {
        System.out.println("Test create server binding on localhost then all interface (0.0.0.0)");

        String osName = System.getProperty("os.name");
        System.out.println("os name: " + osName);

        var errRef = new AtomicReference<Throwable>(null);

        var localhostReceived = new AtomicReference<String>(null);

        var localhostServer = serverManager.getOrCreateJettyServer("localhost:8889", true);
        localhostServer.start();

        localhostServer.addPathHandler("/*", (req, res) -> {
            localhostReceived.set(req.getParameter("key"));
            try (var writer = res.getWriter()) {
                writer.append("");
            } catch (IOException e) {
                errRef.set(e);
            }
        });

        if (osName.equalsIgnoreCase("Mac OS X")) {
            var allInterfaceServer = serverManager.getOrCreateJettyServer("0.0.0.0:8889", true);
            assertNotEquals(localhostServer, allInterfaceServer);
            allInterfaceServer.start();

            final var allInterfaceReceived = new AtomicReference<String>(null);
            allInterfaceServer.addPathHandler("/*", (req, res) -> {
                allInterfaceReceived.set(req.getParameter("key"));
                try (var writer = res.getWriter()) {
                    writer.append("");
                } catch (IOException e) {
                    errRef.set(e);
                }
            });

            final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
            URI uri = new URI("http://localhost:8889/?key=" + encodedText);
            HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
            httpClient.send(request, BodyHandlers.ofString()).body();

            assertNull(allInterfaceReceived.get());
            assertEquals(TEST_TEXT, localhostReceived.get());

            allInterfaceServer.stop();
        }

        assertNull(errRef.get());

        localhostServer.stop();
        System.out.println("*** DONE ***");
    }

    @Test
    public void testMultiPath() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Test handling on multi path");
        JettyHttpServer httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        final CountDownLatch doneSignal = new CountDownLatch(3);

        final AtomicReference<Throwable> exception = new AtomicReference<Throwable>(null);

        JettyRequestHandler handler = (req, res) -> {
            try {
                res.getWriter().write("");
                res.getWriter().flush();
            } catch (IOException e) {
                exception.set(e);
            } finally {
                doneSignal.countDown();
            }
        };

        httpServer.addPathHandler("/*", handler) //
                .addPathHandler("/path1/*", handler) //
                .addPathHandler("/path2/*", handler);

        final String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());

        URI uri = new URI("http://" + address + "/path1/subpath?key=" + encodedText);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        uri = new URI("http://" + address + "/path2/subpath?key=" + encodedText);
        request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        uri = new URI("http://" + address + "/other-path?key=" + encodedText);
        request = HttpRequest.newBuilder().GET().uri(uri).build();
        httpClient.send(request, BodyHandlers.ofString());

        doneSignal.await(5, TimeUnit.SECONDS);

        assertNull(exception.get());

        httpServer.stop();
        System.out.println("*** DONE ***");
    }

    @Test
    public void testPingPong() throws IOException, InterruptedException, URISyntaxException {
        System.out.println("Test ping pong");
        var httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        final var doneSignal = new CountDownLatch(1);
        final var exception = new AtomicReference<Throwable>(null);

        httpServer.addPathHandler("/*", (req, res) -> {
            try (var writer = res.getWriter()) {
                var value = req.getParameter("key");
                writer.write(value);
            } catch (Exception e) {
                exception.set(e);
                doneSignal.countDown();
            }
        });

        final var responseText = new AtomicReference<>(null);

        String encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
        URI uri = new URI("http://" + address + "/?key=" + encodedText);
        HttpRequest request = HttpRequest.newBuilder().GET().uri(uri).build();
        HttpClient httpClient = HttpClient.newHttpClient();
        httpClient.sendAsync(request, BodyHandlers.ofString()).thenAccept((res) -> {
            try {
                responseText.set((String) res.body());
            } finally {
                doneSignal.countDown();
            }
        });

        doneSignal.await(5, TimeUnit.SECONDS);

        assertNull(exception.get());
        assertEquals(TEST_TEXT, responseText.get());

        httpServer.stop();
        System.out.println("*** DONE ***");
    }

    private void echo(HttpServletRequest req, HttpServletResponse res) {
        try {
            var writer = res.getWriter();
            writer.write(req.getParameter("key"));
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Double getSampleValue(String name) {
        return CollectorRegistry.defaultRegistry.getSampleValue(name);
    }

    private Double getSampleValue(String name, String[] labelNames, String[] labelValues) {
        return CollectorRegistry.defaultRegistry.getSampleValue(name, labelNames, labelValues);
    }

    @Test
    public void testPrometheus() throws IOException, InterruptedException, URISyntaxException {
        var prometheusPrefix = "myCustomPrefix";
        var httpServer = serverManager.getOrCreateJettyServer(address, true);
        httpServer.start();

        httpServer.addPathHandler("/prometheus", this::echo, false, true, prometheusPrefix);

        var encodedText = URLEncoder.encode(TEST_TEXT, Charset.defaultCharset().name());
        var uri = new URI("http://" + address + "/prometheus?key=" + encodedText);
        var request = HttpRequest.newBuilder().GET().uri(uri).build();
        var httpClient = HttpClient.newHttpClient();
        httpClient.send(request, BodyHandlers.ofString());

        ThreadUtils.sleep(100);

        assertThat(getSampleValue(prometheusPrefix + "_requests_total"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_requests_active"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_requests_active_max"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_request_time_max_seconds"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_request_time_seconds_total"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_total"), is(1.0));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_active"), is(0.0));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_active_max"), is(greaterThan(0.0)));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_time_max"), is(notNullValue()));
        assertThat(getSampleValue(prometheusPrefix + "_dispatched_time_seconds_total"), is(notNullValue()));
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

        httpServer.stop();
    }
}
