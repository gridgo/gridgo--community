package io.gridgo.connector.jetty.exceptions;

public class PathExistingException extends RuntimeException {

    private static final long serialVersionUID = 2973044521650333297L;

    public PathExistingException() {
        super();
    }

    public PathExistingException(String message) {
        super(message);
    }

    public PathExistingException(String message, Throwable cause) {
        super(message, cause);
    }

    public PathExistingException(Throwable cause) {
        super(cause);
    }
}
