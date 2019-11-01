package io.gridgo.socket.impl;

import static io.gridgo.socket.SocketConnector.DEFAULT_BUFFER_SIZE;
import static io.gridgo.socket.SocketConsumer.DEFAULT_RECV_TIMEOUT;
import static io.gridgo.utils.ThreadUtils.isShuttingDown;
import static io.gridgo.utils.ThreadUtils.sleepSilence;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.Collection;

import io.gridgo.bean.BValue;
import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import io.gridgo.socket.SocketProducer;
import io.gridgo.socket.exceptions.SendMessageException;
import io.gridgo.socket.helper.Endpoint;
import io.gridgo.socket.helper.EndpointParser;
import io.gridgo.utils.helper.Loggable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSocketProducer extends SingleThreadSendingProducer implements SocketProducer, Loggable {

    private static final byte ZERO_BYTE = (byte) 0;

    private final ByteBuffer buffer;

    @Getter
    private long totalSentBytes;

    @Getter
    private long totalSentMessages;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private Receiver receiver;

    private final SocketFactory factory;

    private final SocketOptions options;

    private final String address;

    private Socket socket;

    private boolean useDirectBuffer = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    private Thread monitorThread;

    @Builder(builderClassName = "SocketProducerBuilder")
    private DefaultSocketProducer(//
            ConnectorContext context, //
            SocketFactory factory, //
            SocketOptions options, //
            String address, //
            int bufferSize, //
            Boolean useDirectBuffer, //
            int ringBufferSize, //
            boolean batchingEnabled, //
            int maxBatchSize, //
            Boolean monitorEnabled) {

        super(context, //
                ringBufferSize, //
                r -> new Thread(r, "socket.sender." + address), //
                batchingEnabled, //
                maxBatchSize);

        this.options = options;
        this.factory = factory;
        this.address = address;

        if (useDirectBuffer != null)
            this.useDirectBuffer = useDirectBuffer.booleanValue();

        this.buffer = this.useDirectBuffer//
                ? ByteBuffer.allocateDirect(bufferSize) //
                : ByteBuffer.allocate(bufferSize);

        if (monitorEnabled != null && monitorEnabled.booleanValue())
            monitorThread = new Thread(this::monitor);
    }

    private void monitor() {
        Thread.currentThread().setName(this.generateName() + ".monitor");
        long last = 0;
        var df = new DecimalFormat("###,###.##");
        while (!isShuttingDown()) {
            if (!sleepSilence(1000))
                return;

            long deltaMsgCount = totalSentMessages - last;
            if (deltaMsgCount > 0) {
                log.debug("total sent bytes: {}, total sent msg: {} -> pace: {}", //
                        df.format(totalSentBytes), //
                        df.format(totalSentMessages), //
                        df.format(deltaMsgCount));

                last = totalSentMessages;
            }
        }
    }

    @Override
    protected Message accumulateBatch(@NonNull Collection<Message> messages) {
        if (!this.isBatchingEnabled())
            throw new UnsupportedOperationException("Batching is disabled");

        return SocketUtils.accumulateBatch(messages);
    }

    @Override
    protected void executeSendOnSingleThread(Message message) throws Exception {
        buffer.clear();
        if (options.getType().equalsIgnoreCase("pub")) {
            message.getRoutingId() //
                    .map(BValue::getRaw) //
                    .ifPresent(buffer::put);
            buffer.put(ZERO_BYTE);
        }

        Payload payload = message.getPayload();
        if (payload != null) {
            payload.toBArray().writeBytes(buffer);

            buffer.flip();
            int sentBytes = this.socket.send(buffer);
            if (sentBytes == -1)
                throw new SendMessageException();

            totalSentBytes += sentBytes;
            totalSentMessages++;
        }
    }

    @Override
    protected String generateName() {
        return "producer." + this.getUniqueIdentifier();
    }

    private String getUniqueIdentifier() {
        return new StringBuilder() //
                .append(factory.getType()) //
                .append(".") //
                .append(options.getType()) //
                .append(".") //
                .append(address) //
                .toString();
    }

    @Override
    public boolean isCallSupported() {
        return false;
    }

    @Override
    protected void onStart() {
        this.socket = this.factory.createSocket(options);
        String type = options.getType().trim().toLowerCase();
        switch (type) {
        case "push":
            socket.connect(address);
            break;
        case "pub":
            Endpoint endpoint = EndpointParser.parse(address);
            if (SocketConnector.MULTICAST_TRANSPORTS.contains(endpoint.getProtocol())) {
                socket.connect(address);
            } else {
                socket.bind(address);
            }
            break;
        case "pair":
            socket.connect(address);
            var bufferSizeCfg = options.getConfig().getOrDefault("bufferSize", DEFAULT_BUFFER_SIZE).toString();
            int bufferSize = Integer.parseInt(bufferSizeCfg);

            if (!options.getConfig().containsKey("receiveTimeout"))
                socket.applyConfig("receiveTimeout", DEFAULT_RECV_TIMEOUT);

            this.setReceiver(DefaultSocketReceiver.builder() //
                    .context(getContext()) //
                    .socket(socket) //
                    .bufferSize(bufferSize) //
                    .useDirectBuffer(useDirectBuffer) //
                    .uniqueIdentifier(getUniqueIdentifier()) //
                    .build());
            break;
        default:
        }
        if (this.monitorThread != null)
            this.monitorThread.start();
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.monitorThread != null)
            this.monitorThread.interrupt();
        this.socket.close();
    }
}
