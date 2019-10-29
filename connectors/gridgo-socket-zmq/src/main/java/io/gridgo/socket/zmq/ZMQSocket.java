package io.gridgo.socket.zmq;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.zeromq.ZMQ;

import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.impl.AbstractSocket;
import io.gridgo.utils.PrimitiveUtils;
import io.gridgo.utils.pojo.PojoUtils;
import io.gridgo.utils.pojo.setter.PojoSetterProxy;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ZMQSocket extends AbstractSocket {

    private static final PojoSetterProxy SETTER_PROXY = PojoUtils.getSetterProxy(ZMQ.Socket.class);;
    private static final Map<String, Class<?>> SIGNATURES = new HashMap<>();
    private static final Map<String, String> LOWERCASE_FIELD_NAMES = new HashMap<>();

    static {
        try {
            SETTER_PROXY.getSignatures().forEach(sig -> {
                var fieldName = sig.getFieldName();
                SIGNATURES.put(fieldName, sig.getFieldType());
                LOWERCASE_FIELD_NAMES.put(fieldName.toLowerCase(), fieldName);
            });
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    private final ZMQ.Socket socket;

    @Getter
    private Integer bindingPort = null;

    ZMQSocket(@NonNull ZMQ.Socket socket) {
        this.socket = socket;
    }

    @Override
    public void applyConfig(@NonNull String name, Object value) {
        var fieldName = LOWERCASE_FIELD_NAMES.get(name.toLowerCase());
        if (fieldName == null)
            return;
        log.debug("Applying zmq socket config: {} (orig={})={}", name, fieldName, value);
        var realValue = PrimitiveUtils.getValueFrom(SIGNATURES.get(fieldName), value);
        SETTER_PROXY.applyValue(socket, fieldName, realValue);
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
        int flags = block ? 0 : ZMQ.NOBLOCK;
        return buffer.isDirect() //
                ? socket.recvZeroCopy(buffer, buffer.capacity(), flags) //
                : socket.recvByteBuffer(buffer, flags);
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
