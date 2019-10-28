package io.gridgo.socket.zmq;

import org.zeromq.ZMQ;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ZMQSocket extends AbstractSocket {

    private final ZMQ.Socket socket;

    @Getter
    private Integer bindingPort = null;

    private Map<String, Consumer<String>> setters = new HashMap<>();

    ZMQSocket(@NonNull ZMQ.Socket socket) {
        this.socket = socket;
        this.initializeSetters();
    }

    private void initializeSetters() {
        setters.put("receivetimeout", arg -> socket.setReceiveTimeOut(Integer.parseInt(arg.toString())));
        setters.put("sendtimeout", arg -> socket.setSendTimeOut(Integer.parseInt(arg.toString())));
        setters.put("sndhwm", arg -> socket.setSndHWM(Long.parseLong(arg.toString())));
    }

    @Override
    public void applyConfig(@NonNull String name, Object value) {
        var consumer = setters.get(name.toLowerCase());
        if (consumer == null) {
            log.warn("No setter found for {}", name);
            return;
        }
        consumer.accept(value != null ? value.toString() : null);
    }

    @Override
    protected void doBind(Endpoint endpoint) {
        var resolvedAddress = endpoint.getResolvedAddress();
        if (endpoint.getPort() <= 0) {
            bindingPort = this.socket.bindToRandomPort(resolvedAddress);
        } else {
            this.socket.bind(resolvedAddress);
            this.bindingPort = endpoint.getPort();
        }
        log.debug("success bind to: {}", resolvedAddress);
    }

    @Override
    protected void doClose() {
        this.socket.close();
        this.bindingPort = null;
    }

    @Override
    protected void doConnect(Endpoint endpoint) {
        var resolvedAddress = endpoint.getResolvedAddress();
        this.socket.connect(resolvedAddress);
    }

    @Override
    protected int doReveive(ByteBuffer buffer, boolean block) {
        if (buffer.isDirect()) {
            return this.socket.recvZeroCopy(buffer, buffer.capacity(), block ? 0 : ZMQ.NOBLOCK);
        }
        return this.socket.recvByteBuffer(buffer, ZMQ.NOBLOCK);
    }

    @Override
    protected int doSend(ByteBuffer buffer, boolean block) {
        int flags = block ? 0 : ZMQ.NOBLOCK;
        if (!buffer.isDirect()) {
            int pos = buffer.position();
            int len = buffer.limit() - pos;
            if (this.socket.send(buffer.array(), pos, len, flags)) {
                return len;
            }
            return -1;
        }
        return this.socket.sendByteBuffer(buffer, flags);
    }

    @Override
    public int doSubscribe(@NonNull String topic) {
        this.socket.subscribe(topic.getBytes());
        return 0;
    }
}
