package io.gridgo.connector.http;

import static io.gridgo.connector.http.HttpConstants.IO_THREADS_COUNT;
import static io.gridgo.connector.http.HttpConstants.NAME_RESOLVER_BEAN;
import static io.gridgo.connector.http.HttpConstants.NAME_RESOLVER_CLASS;
import static io.gridgo.connector.http.HttpConstants.PROXY_REALM_BEAN;
import static io.gridgo.connector.http.HttpConstants.SSL_CONTEXT;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.DEFAULT_PROXY_PORT;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_COMPRESSION_SUPPORTED;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_FORMAT;
import static io.gridgo.connector.httpcommon.HttpCommonConstants.PARAM_METHOD;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.CONNECT_TIMEOUT;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.KEEP_ALIVE;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.MAX_CONNECTIONS;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.MAX_REDIRECTS;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.MAX_RETRIES;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.NON_PROXY_HOSTS;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.PROXY_HOST;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.PROXY_PORT;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.PROXY_SECURED_PORT;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.PROXY_TYPE;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.REQUEST_TIMEOUT;
import static io.gridgo.connector.httpcommon.HttpCommonProducerConstants.USE_PROXY;

import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.asynchttpclient.DefaultAsyncHttpClientConfig.Builder;
import org.asynchttpclient.Dsl;
import org.asynchttpclient.Realm;
import org.asynchttpclient.proxy.ProxyServer;
import org.asynchttpclient.proxy.ProxyType;

import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.netty.handler.ssl.SslContext;
import io.netty.resolver.NameResolver;

@ConnectorEndpoint(scheme = "http,https", syntax = "httpUri", raw = true)
public class HttpConnector extends AbstractConnector {

    private static final int DEFAULT_MAX_REDIRECTS = 3;

    private Builder createBuilder() {
        var config = Dsl.config();

        // connection timeout
        var connectTimeout = getParam(CONNECT_TIMEOUT);
        if (connectTimeout != null)
            config.setConnectTimeout(Integer.parseInt(connectTimeout));

        // request timeout
        var requestTimeout = getParam(REQUEST_TIMEOUT);
        if (requestTimeout != null)
            config.setRequestTimeout(Integer.parseInt(requestTimeout));

        // max retries
        var maxRetries = getParam(MAX_RETRIES);
        if (maxRetries != null)
            config.setMaxRequestRetry(Integer.parseInt(maxRetries));

        // max connections
        var maxConnections = getParam(MAX_CONNECTIONS);
        if (maxConnections != null)
            config.setMaxConnections(Integer.parseInt(maxConnections));

        // max redirects
        var maxRedirects = getParam(MAX_REDIRECTS);
        if (maxRedirects != null)
            config.setMaxRedirects(Integer.parseInt(maxRedirects));
        else
            config.setMaxRedirects(DEFAULT_MAX_REDIRECTS);

        // keep-alive
        var keepAlive = getParam(KEEP_ALIVE);
        if (keepAlive != null)
            config.setKeepAlive(Boolean.valueOf(keepAlive));

        // compression
        var compression = getParam(PARAM_COMPRESSION_SUPPORTED);
        if (compression != null)
            config.setCompressionEnforced(Boolean.valueOf(compression));

        // I/O threads count
        var ioThreadsCount = getParam(IO_THREADS_COUNT);
        if (ioThreadsCount != null)
            config.setIoThreadsCount(Integer.parseInt(ioThreadsCount));

        // proxy settings
        var useProxy = getParam(USE_PROXY);
        if (Boolean.valueOf(useProxy)) {
            config.setProxyServer(createProxyServerConfig());
        }

        var sslContextBean = getParam(SSL_CONTEXT);
        if (sslContextBean != null)
            config.setSslContext(getContext().getRegistry().lookupMandatory(sslContextBean, SslContext.class));
        return config;
    }

    private ProxyServer createProxyServerConfig() {
        var host = getParam(PROXY_HOST);
        var port = getParam(PROXY_PORT);
        var securedPort = getParam(PROXY_SECURED_PORT);
        var nonProxyHosts = getParam(NON_PROXY_HOSTS);
        var proxyType = getParam(PROXY_TYPE);
        var realmBean = getParam(PROXY_REALM_BEAN);
        return new ProxyServer( //
                host, //
                port != null ? Integer.parseInt(port) : DEFAULT_PROXY_PORT,
                securedPort != null ? Integer.parseInt(securedPort) : DEFAULT_PROXY_PORT, //
                realmBean != null ? getContext().getRegistry().lookupMandatory(realmBean, Realm.class) : null, //
                nonProxyHosts != null ? Arrays.asList(nonProxyHosts.split(",")) : Collections.emptyList(),
                proxyType != null ? ProxyType.valueOf(proxyType) : ProxyType.HTTP);
    }

    private NameResolver<InetAddress> getNameResolver() {
        var nameResolver = getNameResolverByClass();
        if (nameResolver != null)
            return nameResolver;
        return getNameResolverByBean();
    }

    @SuppressWarnings("unchecked")
    private NameResolver<InetAddress> getNameResolverByBean() {
        var nameResolverBean = getParam(NAME_RESOLVER_BEAN);
        if (nameResolverBean == null)
            return null;
        return getContext().getRegistry().lookupMandatory(nameResolverBean, NameResolver.class);
    }

    @SuppressWarnings("unchecked")
    private NameResolver<InetAddress> getNameResolverByClass() {
        var nameResolverClass = getParam(NAME_RESOLVER_CLASS);
        if (nameResolverClass == null)
            return null;
        try {
            return (NameResolver<InetAddress>) Class.forName(nameResolverClass).getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            throw new RuntimeException(e);
        }
    }

    protected void onInit() {
        var endpoint = getConnectorConfig().getNonQueryEndpoint();
        var config = createBuilder();
        var format = getParam(PARAM_FORMAT);
        var method = getParam(PARAM_METHOD);
        var nameResolver = getNameResolver();
        this.producer = Optional.of(new HttpProducer(getContext(), endpoint, config, format, nameResolver, method));
    }
}
