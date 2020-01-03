package io.gridgo.connector.jetty.server;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum JettyServletContextHandlerOption {

    NO_SESSIONS(0), NO_SECURITY(0), SESSIONS(1), SECURITY(2), GZIP(4);

    @Getter
    private int code;
}
