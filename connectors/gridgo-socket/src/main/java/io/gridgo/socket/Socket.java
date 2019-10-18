package io.gridgo.socket;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Map.Entry;

import io.gridgo.socket.helper.Endpoint;
import lombok.NonNull;

public interface Socket extends Configurable, Bindable {

    default void applyConfig(@NonNull Map<String, Object> options) {
        for (Entry<String, Object> entry : options.entrySet()) {
            this.applyConfig(entry.getKey(), entry.getValue());
        }
    }

    void applyConfig(String name, Object value);

    void close();

    void connect(String address);

    Endpoint getEndpoint();

    boolean isAlive();

    default int receive(ByteBuffer buffer) {
        return this.receive(buffer, true);
    }

    int receive(ByteBuffer buffer, boolean block);

    default int send(byte[] bytes) {
        return this.send(bytes, true);
    }

    default int send(byte[] bytes, boolean block) {
        return this.send(ByteBuffer.wrap(bytes).flip(), block);
    }

    default int send(ByteBuffer message) {
        return this.send(message, true);
    }

    int send(ByteBuffer message, boolean block);

    void subscribe(String topic);
}
