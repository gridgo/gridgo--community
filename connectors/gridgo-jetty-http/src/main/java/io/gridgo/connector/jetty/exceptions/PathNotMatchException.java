package io.gridgo.connector.jetty.exceptions;

public class PathNotMatchException extends RuntimeException {

    private static final long serialVersionUID = 2973044521650333297L;

    public PathNotMatchException() {
        super();
    }

    public PathNotMatchException(String message) {
        super(message);
    }

    public PathNotMatchException(String message, Throwable cause) {
        super(message, cause);
    }

    public PathNotMatchException(Throwable cause) {
        super(cause);
    }
}
