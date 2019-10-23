package io.gridgo.socket;

import java.util.Map;
import java.util.Map.Entry;

import lombok.NonNull;

public interface Configurable {

    default void applyConfig(@NonNull Map<String, Object> options) {
        for (Entry<String, Object> entry : options.entrySet()) {
            this.applyConfig(entry.getKey(), entry.getValue());
        }
    }

    void applyConfig(String name, Object value);
}
