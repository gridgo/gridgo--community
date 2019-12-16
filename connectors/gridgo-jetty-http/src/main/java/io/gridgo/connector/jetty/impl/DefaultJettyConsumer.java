package io.gridgo.connector.jetty.impl;

import java.util.Set;
import java.util.function.Function;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joo.promise4j.Deferred;
import org.joo.promise4j.impl.CompletableDeferredObject;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.jetty.JettyConsumer;
import io.gridgo.connector.jetty.JettyResponder;
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
            Set<JettyServletContextHandlerOption> options) {

        super(context);

        this.options = options;
        this.address = address;
        this.requestParser = HttpRequestParser.newDefault(format);

        path = (path == null || path.isBlank()) ? "/*" : path.trim();
        this.path = path.startsWith("/") ? path : ("/" + path);

        httpServer = SERVER_MANAGER.getOrCreateJettyServer(address, http2Enabled, options);
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
            requestMessage = this.requestParser.parse(request, this.options);
            var deferredAndRoutingId = getJettyResponder().registerRequest(request);
            this.publish(requestMessage.setRoutingIdFromAny(deferredAndRoutingId.getRoutingId()),
                    deferredAndRoutingId.getDeferred());
        } catch (Exception e) {
            getLogger().error("error while handling http request", e);
            onUncaughtException(e, response);
        }
    }

    private void onUncaughtException(Throwable e, HttpServletResponse response) {
        Message responseMessage = this.failureHandler != null ? this.failureHandler.apply(e)
                : this.getJettyResponder().generateFailureMessage(e);
        ((JettyResponder) this.getResponder()).writeResponse(response, responseMessage);
    }

    @Override
    protected void onStart() {
        this.httpServer.start();
        this.httpServer.addPathHandler(this.path, this::onHttpRequest, this::onUncaughtException);
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