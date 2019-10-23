package io.gridgo.socket;

import java.util.HashMap;
import java.util.Map;

import lombok.Data;
import lombok.NonNull;

@Data
public class SocketOptions {

    private @NonNull String type;

    private final Map<String, Object> config = new HashMap<>();

    public SocketOptions addConfig(String name, Object value) {
        this.config.put(name, value);
        return this;
    }
}
