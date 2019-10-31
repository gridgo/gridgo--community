package io.gridgo.socket.impl;

import static io.gridgo.socket.impl.SocketUtils.startPolling;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.Socket;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultSocketReceiver extends AbstractConsumer
        implements Receiver, FailureHandlerAware<DefaultSocketReceiver> {

    private Thread poller;
    private final Socket socket;
    private final int bufferSize;
    private CountDownLatch doneSignal;
    private final String uniqueIdentifier;

    @Getter
    private long totalRecvBytes;

    @Getter
    private long totalRecvMessages;

    private boolean useDirectBuffer = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    @Getter(AccessLevel.PROTECTED)
    private Function<Throwable, Message> failureHandler;

    @Builder
    private DefaultSocketReceiver(ConnectorContext context, Socket socket, int bufferSize, Boolean useDirectBuffer,
            String uniqueIdentifier) {
        super(context);
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.uniqueIdentifier = uniqueIdentifier;

        if (useDirectBuffer != null)
            this.useDirectBuffer = useDirectBuffer.booleanValue();

        this.setFailureHandler(context.getExceptionHandler());
    }

    @Override
    public DefaultSocketReceiver setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

    @Override
    protected String generateName() {
        return "receiver." + this.uniqueIdentifier;
    }

    @Override
    protected void onStart() {
        this.totalRecvBytes = 0;
        this.totalRecvMessages = 0;

        this.doneSignal = new CountDownLatch(1);
        this.poller = new Thread(() -> {
            var buffer = useDirectBuffer //
                    ? ByteBuffer.allocateDirect(bufferSize)
                    : ByteBuffer.allocate(bufferSize);

            startPolling(socket, buffer, false, //
                    this::handleSocketMessage, // message handler
                    this::increaseTotalRecvBytes, // callback to update total recv bytes
                    this::increaseTotalRecvMsgs, // callback to update total recv msg count
                    getContext().getExceptionHandler()); // exception handler

            // close socket right after the polling thread got interupted
            socket.close();
            doneSignal.countDown();
        }, socket.getEndpoint().getAddress() + ".poller");

        this.poller.start();
    }

    @Override
    protected void onStop() {
        this.poller.interrupt();
        this.poller = null;

        try {
            this.doneSignal.await();
            this.doneSignal = null;
        } catch (InterruptedException e) {
            log.error("Error while await for socket to close", e);
        }
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

}
