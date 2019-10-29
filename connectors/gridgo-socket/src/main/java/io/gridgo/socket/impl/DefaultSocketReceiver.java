package io.gridgo.socket.impl;

import static io.gridgo.socket.impl.SocketUtils.startPolling;

import java.nio.ByteBuffer;
import java.util.concurrent.CountDownLatch;
import java.util.function.Function;

import io.gridgo.connector.Receiver;
import io.gridgo.connector.impl.AbstractConsumer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.Socket;
import lombok.AccessLevel;
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

    @Getter(AccessLevel.PROTECTED)
    private Function<Throwable, Message> failureHandler;

    public DefaultSocketReceiver(ConnectorContext context, Socket socket, int bufferSize, String uniqueIdentifier) {
        super(context);
        this.socket = socket;
        this.bufferSize = bufferSize;
        this.uniqueIdentifier = uniqueIdentifier;
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
            var buffer = ByteBuffer.allocateDirect(bufferSize);

            startPolling(socket, buffer, false, //
                    this::handleSocketMessage, // message handler
                    this::increaseTotalRecvBytes, // callback to update total recv bytes
                    this::increaseTotalRecvMsgs, // callback to update total recv msg count
                    getContext().getExceptionHandler()); // exception handler

            // close socket right after the polling thread got interupted
            socket.close();
            doneSignal.countDown();
        }, socket.getEndpoint().getAddress() + " POLLER");

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
