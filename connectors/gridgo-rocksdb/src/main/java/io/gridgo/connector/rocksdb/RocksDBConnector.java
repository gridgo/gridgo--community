package io.gridgo.connector.rocksdb;

import java.util.Optional;

import org.rocksdb.RocksDB;

import io.gridgo.connector.DataSourceProvider;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "rocksdb", syntax = "//{path}", category = "keyvalue")
public class RocksDBConnector extends AbstractConnector implements DataSourceProvider<RocksDB> {

    static {
        RocksDB.loadLibrary();
    }

    protected void onInit() {
        var path = getPlaceholder("path");
        this.producer = Optional.of(new RocksDBProducer(getContext(), getConnectorConfig(), path));
    }

    @Override
    public Optional<RocksDB> getDataSource() {
        return this.producer.map(p -> ((RocksDBProducer) p).getDb());
    }
}
