package io.gridgo.extras.etcd.support.exceptions;

public class EtcdException extends RuntimeException {

    private static final long serialVersionUID = -1978405767391372957L;

    public EtcdException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
