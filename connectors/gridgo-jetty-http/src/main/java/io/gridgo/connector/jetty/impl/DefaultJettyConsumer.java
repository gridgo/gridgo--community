package io.gridgo.connector.jetty.impl;

import java.nio.charset.Charset;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.http.HttpMethod;
import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.parser.DefaultHttpRequestParser;
import io.gridgo.connector.jetty.parser.HttpRequestParser;
import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.connector.jetty.support.PathMatcher;
import io.gridgo.connector.support.DeferredAndRoutingId;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.support.HostAndPort;
import io.prometheus.client.Histogram;
import lombok.Builder;
import lombok.NonNull;

public class DefaultJettyConsumer extends AbstractHasResponderConsumer implements JettyConsumer {

    private JettyHttpServer httpServer;
    private final HttpRequestParser requestParser;
    private Function<Throwable, Message> failureHandler;

    private final String path;
    private final String uniqueIdentifier;
    private final boolean enablePrometheus;
    private final String prometheusPrefix;
    private final HttpMethod[] methods;

    private final Histogram latenciesMetric;

    @Builder
    private DefaultJettyConsumer(//
            ConnectorContext context, //
            @NonNull HostAndPort address, //
            boolean http2Enabled, //
            boolean mmapEnabled, //
            String format, //
            String path, //
            String charsetName, //
            Integer stringBufferSize, //
            Boolean enablePrometheus, //
            String prometheusPrefix, //
            String pathSeparator, //
            Boolean caseSensitiveOnMatchingPath, //
            Boolean trimTokensOnMatchingPath, //
            HttpMethod... methods) {

        super(context);

        var pathMatcher = PathMatcher.builder() //
                .pathSeparator(pathSeparator) //
                .caseSensitive(caseSensitiveOnMatchingPath) //
                .trimTokens(trimTokensOnMatchingPath) //
                .build();
        httpServer = JettyHttpServerManager.getInstance().getOrCreateJettyServer(address, http2Enabled, pathMatcher);

        if (httpServer == null)
            throw new RuntimeException("Cannot create http server for address: " + address);

        this.requestParser = DefaultHttpRequestParser.builder() //
                .charset(charsetName == null ? null : Charset.forName(charsetName)) //
                .stringBufferSize(stringBufferSize) //
                .format(format) //
                .build();

        path = (path == null || path.isBlank()) ? "/*" : path.trim();
        this.path = path.startsWith("/") ? path : ("/" + path);

        this.uniqueIdentifier = address.toHostAndPort() + this.path;

        this.enablePrometheus = enablePrometheus == null ? false : enablePrometheus.booleanValue();
        this.prometheusPrefix = prometheusPrefix == null ? uniqueIdentifier : prometheusPrefix;

        this.latenciesMetric = this.enablePrometheus //
                ? Histogram.build() //
                        .name(this.prometheusPrefix + "_requests_latency_seconds") //
                        .help("Request latency in seconds") //
                        .register() //
                : null;

        this.setResponder(DefaultJettyResponder.builder() //
                .format(format) //
                .context(getContext()) //
                .mmapEnabled(mmapEnabled) //
                .uniqueIdentifier(uniqueIdentifier) //
                .build());

        this.methods = methods;
    }

    protected Deferred<Message, Exception> createDeferred() {
        return new CompletableDeferredObject<>();
    }

    @Override
    protected String generateName() {
        return "consumer.jetty.http-server." + this.uniqueIdentifier;
    }

    protected JettyResponder getJettyResponder() {
        return (JettyResponder) this.getResponder();
    }

    private void onHttpRequest(HttpServletRequest request, HttpServletResponse response) {
        Message requestMessage = null;
        DeferredAndRoutingId dnr = null;

        var timer = latenciesMetric != null ? latenciesMetric.startTimer() : null;
        try {
            // parse http servlet request to message object
            requestMessage = requestParser.parse(request);
            dnr = getJettyResponder().registerRequest(request);
        } catch (Exception e) {
            getLogger().error("error while handling http request", e);
            onUncaughtException(e, response);
        } finally {
            if (timer != null && dnr != null)
                dnr.getDeferred().promise().always((stt, res, ex) -> timer.close());
        }

        if (dnr != null) {
            try {
                publish(requestMessage.setRoutingIdFromAny(dnr.getRoutingId()), dnr.getDeferred());
            } catch (Exception e) {
                dnr.getDeferred().reject(e);
            }
        }
    }

    private void onUncaughtException(Throwable e, HttpServletResponse response) {
        var responseMessage = failureHandler != null //
                ? failureHandler.apply(e) //
                : getJettyResponder().generateFailureMessage(e);

        getJettyResponder().writeResponse(response, responseMessage);
    }

    @Override
    protected void onStart() {
        httpServer.addPathHandler(path, this::onHttpRequest, enablePrometheus, prometheusPrefix, methods);
        httpServer.start();
    }

    @Override
    protected void onStop() {
        this.httpServer.stop();
    }

    @Override
    public JettyConsumer setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        getJettyResponder().setFailureHandler(failureHandler);
        return this;
    }
}