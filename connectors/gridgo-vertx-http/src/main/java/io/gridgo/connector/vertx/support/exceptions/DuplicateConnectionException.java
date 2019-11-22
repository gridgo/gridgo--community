package io.gridgo.connector.vertx.support.exceptions;

public class DuplicateConnectionException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateConnectionException(String connectionKey) {
        super(connectionKey);
    }
}
