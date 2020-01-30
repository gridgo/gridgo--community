package io.gridgo.connector.jetty.server;

import static io.gridgo.connector.jetty.server.StatisticsCollector.newStatisticsCollector;
import static org.eclipse.jetty.servlet.ServletContextHandler.GZIP;

import java.util.function.Consumer;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.eclipse.jetty.server.handler.StatisticsHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.utils.support.HostAndPort;
import lombok.Builder;
import lombok.NonNull;

public class JettyHttpServer extends NonameComponentLifecycle {

    private Server server;
    private final Consumer<HostAndPort> onStopCallback;

    private final HostAndPort address;
    private final boolean http2Enabled;

    private final HandlerCollection handlers = new HandlerCollection(true);

    @Builder
    private JettyHttpServer( //
            @NonNull HostAndPort address, //
            boolean http2Enabled, //
            Consumer<HostAndPort> onStopCallback) {

        this.address = address;
        this.http2Enabled = http2Enabled;
        this.onStopCallback = onStopCallback;
    }

    public JettyHttpServer addPathHandler(String path, JettyRequestHandler handler) {
        return addPathHandler(path, handler, false, false, null);
    }

    public JettyHttpServer addPathHandler( //
            @NonNull String path, //
            @NonNull JettyRequestHandler handler, //
            boolean enableGzip, //
            boolean enablePrometheus, //
            String prometheusPrefix) {

        var servletHolder = new ServletHolder(DelegatingServlet.of(handler));
        servletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(path));

        var ctx = new ServletContextHandler(enableGzip ? GZIP : 0);
        ctx.addServlet(servletHolder, path);

        Handler _handler = ctx;

        if (enablePrometheus) {
            var statsHandler = new StatisticsHandler();
            statsHandler.setHandler(_handler);
            // register collector
            newStatisticsCollector(statsHandler, prometheusPrefix).register();
            _handler = statsHandler;
        }

        handlers.addHandler(_handler);

        if (isStarted()) {
            try {
                _handler.start();
            } catch (Exception e) {
                throw new RuntimeException("Cannot start new handler", e);
            }
        }

        return this;
    }

    @Override
    protected void onStart() {
        server = new Server();
        ServerConnector connector;

        var config = new HttpConfiguration();
        var http1 = new HttpConnectionFactory(config);

        if (http2Enabled) {
            var http2c = new HTTP2CServerConnectionFactory(config);
            connector = new ServerConnector(server, http1, http2c);
        } else {
            connector = new ServerConnector(server, http1);
        }

        connector.setHost(address.getResolvedIp());
        connector.setPort(address.getPort());

        server.addConnector(connector);
        server.setHandler(handlers);

        ((QueuedThreadPool) server.getThreadPool()).setName(this.getName());

        try {
            server.start();
        } catch (Exception e) {
            throw new RuntimeException("Cannot start server connector for host: " + address, e);
        }
    }

    @Override
    protected void onStop() {
        try {
            this.server.stop();
        } catch (Exception e) {
            getLogger().error("Error while stop jetty server: " + getName(), e);
        } finally {
            if (this.onStopCallback != null) {
                this.onStopCallback.accept(this.address);
            }
            this.server = null;
        }
    }

}
