package io.gridgo.xrpc.exception;

public class XrpcException extends RuntimeException {

    private static final long serialVersionUID = 2145646758595956264L;

    public XrpcException() {
        super();
    }

    public XrpcException(String message) {
        super(message);
    }

    public XrpcException(String message, Throwable cause) {
        super(message, cause);
    }

    public XrpcException(Throwable cause) {
        super(cause);
    }

}
