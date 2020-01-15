package io.gridgo.socket.zmq;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import org.zeromq.ZMQ;

import io.gridgo.socket.impl.AbstractSocket;
import io.gridgo.utils.PrimitiveUtils;
import io.gridgo.utils.pojo.PojoUtils;
import io.gridgo.utils.pojo.setter.PojoSetterProxy;
import io.gridgo.utils.support.Endpoint;
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

        if (fieldName != null) {
            doApplyConfig(fieldName, value);
            return;
        }

        if ("buffersize".equalsIgnoreCase(name)) {
            doApplyConfig("sendBufferSize", value);
            doApplyConfig("receiveBufferSize", value);
        }
    }

    private void doApplyConfig(String fieldName, Object value) {
        var realValue = PrimitiveUtils.getValueFrom(SIGNATURES.get(fieldName), value);
        SETTER_PROXY.applyValue(socket, fieldName, realValue);

        if (log.isDebugEnabled())
            log.debug("Applied zmq socket config: {}={}", fieldName, realValue);
    }

    @Override
    protected void doBind(Endpoint endpoint) {
        var resolvedAddress = endpoint.getResolvedAddress();
        if (!endpoint.getProtocol().equalsIgnoreCase("ipc") && endpoint.getPort() <= 0) {
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
        if (buffer.isDirect())
            return socket.recvByteBuffer(buffer, flags);

        int offset = buffer.position();
        int maxLength = buffer.capacity() - offset;
        int rc = socket.recv(buffer.array(), offset, maxLength, flags);
        if (rc >= 0)
            buffer.position(offset + rc);

        return rc;
    }

    @Override
    protected int doSend(ByteBuffer buffer, boolean block) {
        int flags = block ? 0 : ZMQ.NOBLOCK;

        if (buffer.isDirect())
            return socket.sendByteBuffer(buffer, flags);

        int pos = buffer.position();
        int len = buffer.limit() - pos;
        return socket.send(buffer.array(), pos, len, flags) ? len : -1;
    }

    @Override
    public int doSubscribe(@NonNull String topic) {
        this.socket.subscribe(topic.getBytes());
        return 0;
    }
}
