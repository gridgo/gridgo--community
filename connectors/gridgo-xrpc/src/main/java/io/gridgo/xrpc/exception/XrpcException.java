package io.gridgo.xrpc.exception;

import io.gridgo.bean.BElement;
import lombok.Getter;

public class XrpcException extends RuntimeException {

    private static final long serialVersionUID = 2145646758595956264L;

    @Getter
    private final BElement body;

    public XrpcException(BElement body) {
        super();
        this.body = body;
    }

    public XrpcException(BElement body, String message) {
        super(message);
        this.body = body;
    }

    public XrpcException(BElement body, String message, Throwable cause) {
        super(message, cause);
        this.body = body;
    }

    public XrpcException(BElement body, Throwable cause) {
        super(cause);
        this.body = body;
    }
}
