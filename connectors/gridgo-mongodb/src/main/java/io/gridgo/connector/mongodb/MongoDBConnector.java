package io.gridgo.connector.mongodb;

import java.util.Optional;

import org.bson.Document;

import com.mongodb.async.client.MongoCollection;

import io.gridgo.connector.DataSourceProvider;
import io.gridgo.connector.impl.AbstractConnector;
import io.gridgo.connector.support.annotations.ConnectorEndpoint;

@ConnectorEndpoint(scheme = "mongodb", syntax = "{connectionBean}/{database}[/{collection}]")
public class MongoDBConnector extends AbstractConnector implements DataSourceProvider<MongoCollection<Document>> {

    protected void onInit() {
        var connectionBean = getPlaceholder("connectionBean").toString();
        var database = getPlaceholder("database").toString();
        var collection = getPlaceholder("collection");
        var collectionName = collection != null ? collection.toString() : null;

        this.producer = Optional.of(new MongoDBProducer(getContext(), connectionBean, database, collectionName));
    }

    @Override
    public Optional<MongoCollection<Document>> getDataSource() {
        return this.producer.map(p -> ((MongoDBProducer) p).getCollection());
    }
}
