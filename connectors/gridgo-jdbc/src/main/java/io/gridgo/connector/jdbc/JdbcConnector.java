package io.gridgo.connector.jdbc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;

import org.jdbi.v3.core.ConnectionFactory;
import org.jdbi.v3.core.Jdbi;

import com.zaxxer.hikari.HikariDataSource;

import io.gridgo.connector.DataSourceProvider;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;
import io.gridgo.framework.support.exceptions.BeanNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ConnectorEndpoint(scheme = "jdbc", syntax = "jdbcUri", raw = true, category = "jdbc")
public class JdbcConnector extends AbstractConnector implements DataSourceProvider<Jdbi> {

    private HikariDataSource connectionPool;

    private static HashSet<String> reserveParams = new HashSet<>(Arrays.asList("pool"));

    @Override
    protected void onInit() {
        var userName = getParam("user");
        var password = extractPassword();
        var connectionFactory = extractConnectionPool(userName, password);
        this.producer = Optional.of(new JdbcProducer(getContext(), connectionFactory));
    }

    private ConnectionFactory extractConnectionPool(String userName, String password) {
        var connectionBean = getParam("pool");
        if (connectionBean == null)
            return initialDefaulConnectionFactory(extractJdbcUrl(), userName, password);
        try {
            return getContext().getRegistry().lookupMandatory(connectionBean, ConnectionFactory.class);
        } catch (BeanNotFoundException ex) {
            log.error("Didn't find appropriate pool", ex);
            throw ex;
        }
    }

    private String extractPassword() {
        var password = getParam("password");
        if (password == null) {
            var passwordKey = getParam("passwordKey");
            if (passwordKey != null)
                password = getContext().getRegistry().lookup(passwordKey, String.class);
        }
        return password;
    }

    private ConnectionFactory initialDefaulConnectionFactory(String jdbcUrl, String userName, String password) {
        this.connectionPool = new HikariDataSource();
        connectionPool.setJdbcUrl(jdbcUrl);
        connectionPool.setUsername(userName);
        connectionPool.setPassword(password);
        return connectionPool::getConnection;
    }

    private String extractJdbcUrl() {
        // exclude param of gridgo
        var params = getConnectorConfig().getParameters().entrySet().stream()//
                                         .filter(entry -> !reserveParams.contains(entry.getKey())) //
                                         .map(entry -> entry.getKey() + "=" + entry.getValue()) //
                                         .reduce((p1, p2) -> p1 + "&" + p2) //
                                         .orElse("");
        return getConnectorConfig().getNonQueryEndpoint() + (params.isEmpty() ? "" : "?" + params);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.connectionPool != null)
            this.connectionPool.close();
    }

    @Override
    public Optional<Jdbi> getDataSource() {
        return this.producer.map(p -> ((JdbcProducer) p).getJdbiClient());
    }
}
