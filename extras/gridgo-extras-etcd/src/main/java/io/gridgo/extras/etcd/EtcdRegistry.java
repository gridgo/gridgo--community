package io.gridgo.extras.etcd;

import java.util.concurrent.ExecutionException;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.gridgo.bean.BElement;
import io.gridgo.extras.etcd.support.exceptions.EtcdException;
import io.gridgo.framework.support.Registry;
import lombok.NonNull;

public class EtcdRegistry implements Registry {

    private static final String DEFAULT_HOST = "http://localhost:2379";

    private Client client;

    public EtcdRegistry() {
        this(DEFAULT_HOST);
    }

    public EtcdRegistry(String... endpoints) {
        this.client = Client.builder().endpoints(endpoints).build();
    }

    public EtcdRegistry(Client client) {
        this.client = client;
    }

    @Override
    public <T> T convertAnswer(Class<T> type, Object answer) {
        if (BElement.class.isAssignableFrom(type)) {
            return BElement.ofBytes((byte[]) answer);
        }
        return Registry.super.convertAnswer(type, answer);
    }

    @Override
    public Object lookup(String name) {
        var key = createByteSequence(name);
        try {
            var response = client.getKVClient().get(key).get();
            var kvs = response.getKvs();
            if (kvs == null || kvs.isEmpty())
                return null;
            return kvs.get(0).getValue().getBytes();
        } catch (InterruptedException | ExecutionException e) {
            throw new EtcdException("Exception caught while retrieving key from Etcd: " + name, e);
        }
    }

    @Override
    public Registry register(String name, @NonNull Object answer) {
        var key = createByteSequence(name);
        var value = convertValue(answer);
        try {
            client.getKVClient().put(key, value).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new EtcdException("Exception caught while updating key from Etcd: " + name, e);
        }
        return this;
    }

    private ByteSequence convertValue(Object answer) {
        if (answer instanceof byte[])
            return ByteSequence.from((byte[]) answer);
        if (answer instanceof BElement)
            return ByteSequence.from(((BElement) answer).toBytes());
        return ByteSequence.from(answer.toString().getBytes());
    }

    private ByteSequence createByteSequence(String name) {
        return ByteSequence.from(name.getBytes());
    }
}
