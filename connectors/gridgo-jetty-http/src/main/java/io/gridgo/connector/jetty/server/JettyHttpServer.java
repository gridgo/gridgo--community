package io.gridgo.connector.jetty.server;

import java.util.Set;
import java.util.function.Consumer;

import javax.servlet.MultipartConfigElement;

import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import io.gridgo.framework.impl.NonameComponentLifecycle;
import io.gridgo.utils.support.HostAndPort;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class JettyHttpServer extends NonameComponentLifecycle {

    private Server server;

    @Getter
    private final HostAndPort address;

    private ServletContextHandler handler;

    private final Consumer<HostAndPort> onStopCallback;

    private final Set<JettyServletContextHandlerOption> options;

    private final boolean http2Enabled;

    @Builder
    private JettyHttpServer( //
            @NonNull HostAndPort address, //
            boolean http2Enabled, //
            Set<JettyServletContextHandlerOption> options, //
            Consumer<HostAndPort> onStopCallback) {

        this.address = address;
        this.options = options;
        this.http2Enabled = http2Enabled;
        this.onStopCallback = onStopCallback;
    }

    public JettyHttpServer addPathHandler( //
            @NonNull String path, //
            @NonNull JettyRequestHandler handler) {

        var servletHolder = new ServletHolder(new DelegatingServlet(handler));
        servletHolder.getRegistration().setMultipartConfig(new MultipartConfigElement(path));

        this.handler.addServlet(servletHolder, path);
        return this;
    }

    private ServletContextHandler createServletContextHandler() {
        if (options == null || options.size() == 0)
            return new ServletContextHandler();

        int accumulateOptions = 0;
        for (var option : options)
            accumulateOptions = accumulateOptions | option.getCode();

        return new ServletContextHandler(accumulateOptions);
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

        handler = createServletContextHandler();
        server.setHandler(handler);

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
            this.handler = null;
        }
    }

}
