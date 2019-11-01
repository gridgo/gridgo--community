package io.gridgo.socket.impl;

import static io.gridgo.socket.impl.SocketUtils.startPolling;
import static io.gridgo.utils.ThreadUtils.isShuttingDown;
import static io.gridgo.utils.ThreadUtils.sleepSilence;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;

import io.gridgo.connector.impl.AbstractHasResponderConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.Socket;
import io.gridgo.socket.SocketConnector;
import io.gridgo.socket.SocketConsumer;
import io.gridgo.socket.SocketFactory;
import io.gridgo.socket.SocketOptions;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSocketConsumer extends AbstractHasResponderConsumer implements SocketConsumer {

    @Getter
    private long totalRecvBytes;

    @Getter
    private long totalRecvMessages;

    private Thread poller;

    private final int bufferSize;

    private final SocketFactory factory;

    private final SocketOptions options;

    private final String address;

    private CountDownLatch doneSignal;

    private boolean autoSkipTopicHeader = false;

    private boolean useDirectBuffer = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    @Getter
    private Integer bindingPort;

    private final Thread monitorThread;

    @Builder(builderClassName = "SocketConsumerBuidler")
    private DefaultSocketConsumer(ConnectorContext context, //
            SocketFactory factory, //
            SocketOptions options, //
            String address, //
            int bufferSize, //
            Boolean useDirectBuffer, //
            Boolean monitorEnabled) {
        super(context);
        this.factory = factory;
        this.options = options;
        this.address = address;
        this.bufferSize = bufferSize;

        if (useDirectBuffer != null)
            this.useDirectBuffer = useDirectBuffer.booleanValue();

        if (monitorEnabled != null && monitorEnabled.booleanValue()) {
            monitorThread = new Thread(this::monitor);
        } else {
            monitorThread = null;
        }
    }

    private void monitor() {
        String name = this.generateName();
        Thread.currentThread().setName(name + ".monitor");
        log.info("start monitoring socket consumer: {}", name);
        long last = 0;
        var df = new DecimalFormat("###,###.##");
        while (!isShuttingDown()) {
            if (!sleepSilence(1000))
                return;

            long deltaMsgCount = totalRecvMessages - last;
            if (deltaMsgCount > 0) {
                log.debug("total received bytes: {}, total received msg: {} -> pace: {}", //
                        df.format(totalRecvBytes), //
                        df.format(totalRecvMessages), //
                        df.format(deltaMsgCount));

                last = totalRecvMessages;
            }
        }
    }

    @Override
    protected String generateName() {
        return "consumer." + this.getUniqueIdentifier();
    }

    private String getUniqueIdentifier() {
        return new StringBuilder() //
                .append(this.factory.getType()) //
                .append(".") //
                .append(this.options.getType()) //
                .append(".") //
                .append(this.address) //
                .toString();
    }

    private Socket initSocket() {
        Socket socket = this.factory.createSocket(options);
        if (!options.getConfig().containsKey("receiveTimeout")) {
            socket.applyConfig("receiveTimeout", DEFAULT_RECV_TIMEOUT);
        }

        switch (options.getType().toLowerCase()) {
        case "pull":
            socket.bind(address);
            this.bindingPort = socket.getBindingPort();
            break;
        case "sub":
            socket.connect(address);
            String topic = (String) options.getConfig().getOrDefault("topic", "");
            socket.subscribe(topic);
            this.autoSkipTopicHeader = true;
            break;
        case "pair":
            socket.bind(address);
            this.bindingPort = socket.getBindingPort();
            int maxBatchSize = 0;
            boolean batchingEnabled = Boolean.parseBoolean((String) this.options.getConfig().get("batchingEnabled"));
            if (batchingEnabled) {
                maxBatchSize = Integer.valueOf((String) this.options.getConfig().getOrDefault("maxBatchingSize",
                        SocketConnector.DEFAULT_MAX_BATCH_SIZE));
            }
            this.setResponder(DefaultSocketResponder.builder() //
                    .context(getContext()) //
                    .socket(socket) //
                    .bufferSize(bufferSize) //
                    .ringBufferSize(2048) //
                    .batchingEnabled(batchingEnabled) //
                    .maxBatchSize(maxBatchSize) //
                    .uniqueIdentifier(getUniqueIdentifier()) //
                    .useDirectBuffer(useDirectBuffer) //
                    .build());
            // new DefaultSocketResponder(getContext(), socket, bufferSize, 1024,
            // batchingEnabled,
            // maxBatchSize, this.getUniqueIdentifier())
            break;
        default:
        }
        return socket;
    }

    @Override
    protected void onStart() {

        var socket = initSocket();
        this.totalRecvBytes = 0;
        this.totalRecvMessages = 0;

        this.doneSignal = new CountDownLatch(1);
        this.poller = new Thread(() -> {
            var buffer = socket.forceUsingDirectBuffer() || useDirectBuffer //
                    ? ByteBuffer.allocateDirect(bufferSize) //
                    : ByteBuffer.allocate(bufferSize);

            log.debug("****** Using {} byte buffer", useDirectBuffer ? "direct" : "heap");

            startPolling(socket, buffer, autoSkipTopicHeader, //
                    this::handleSocketMessage, //
                    this::increaseTotalRecvBytes, //
                    this::increaseTotalRecvMsgs, //
                    getContext().getExceptionHandler()); // poller will do looping here until stop() called, mean this
                                                         // thread got interupted

            socket.close(); // close socket right after poll method escaped
            doneSignal.countDown();
        }, this.getName() + ".poller");

        this.poller.start();
        if (this.monitorThread != null)
            this.monitorThread.start();
    }

    private void handleSocketMessage(Message message) {
        ensurePayloadId(message);
        publish(message, null);
    }

    private void increaseTotalRecvBytes(long recvBytes) {
        totalRecvBytes += recvBytes;
    }

    private void increaseTotalRecvMsgs(long recvMsgs) {
        totalRecvMessages += recvMsgs;
    }

    @Override
    protected final void onStop() {
        if (this.monitorThread != null)
            this.monitorThread.interrupt();

        if (this.poller == null || this.poller.isInterrupted())
            return;

        this.poller.interrupt();
        this.poller = null;

        try {
            this.doneSignal.await();
        } catch (InterruptedException e) {
            log.error("Error while waiting for socket to be closed", e);
        } finally {
            this.doneSignal = null;
        }

        this.bindingPort = null;
    }
}
