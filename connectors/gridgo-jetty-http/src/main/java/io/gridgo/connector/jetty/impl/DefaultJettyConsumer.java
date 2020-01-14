package io.gridgo.connector.jetty.impl;

import java.nio.charset.Charset;
import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
import io.gridgo.connector.jetty.parser.DefaultHttpRequestParser;
import io.gridgo.connector.jetty.parser.HttpRequestParser;
import io.gridgo.connector.jetty.server.JettyHttpServer;
import io.gridgo.connector.jetty.server.JettyHttpServerManager;
import io.gridgo.connector.jetty.server.JettyServletContextHandlerOption;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.utils.support.HostAndPort;
import lombok.Builder;
import lombok.NonNull;

public class DefaultJettyConsumer extends AbstractHasResponderConsumer implements JettyConsumer {

    private static final JettyHttpServerManager SERVER_MANAGER = JettyHttpServerManager.getInstance();

    private final HttpRequestParser requestParser;

    private final String path;
    private final HostAndPort address;

    private JettyHttpServer httpServer;
    private Function<Throwable, Message> failureHandler;

    private final String uniqueIdentifier;

    private final Set<JettyServletContextHandlerOption> options;

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
            Set<JettyServletContextHandlerOption> options, //
            Boolean enablePrometheus, String prometheusPrefix) {

        super(context);

        this.options = options;
        this.address = address;
        this.requestParser = DefaultHttpRequestParser.builder() //
                .charset(charsetName == null ? null : Charset.forName(charsetName)) //
                .stringBufferSize(stringBufferSize) //
                .format(format) //
                .build();

        path = (path == null || path.isBlank()) ? "/*" : path.trim();
        this.path = path.startsWith("/") ? path : ("/" + path);

        httpServer = SERVER_MANAGER.getOrCreateJettyServer(address, http2Enabled, options,
                enablePrometheus == null ? false : enablePrometheus, prometheusPrefix);
        if (httpServer == null)
            throw new RuntimeException("Cannot create http server for address: " + this.address);

        this.uniqueIdentifier = address.toHostAndPort() + this.path;

        this.setResponder(DefaultJettyResponder.builder() //
                .format(format) //
                .context(getContext()) //
                .mmapEnabled(mmapEnabled) //
                .uniqueIdentifier(uniqueIdentifier) //
                .build());
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
        try {
            // parse http servlet request to message object
            requestMessage = requestParser.parse(request, options);
            var dnr = getJettyResponder().registerRequest(request);
            publish(requestMessage.setRoutingIdFromAny(dnr.getRoutingId()), dnr.getDeferred());
        } catch (Exception e) {
            getLogger().error("error while handling http request", e);
            onUncaughtException(e, response);
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
        httpServer.start();
        httpServer.addPathHandler(path, this::onHttpRequest);
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