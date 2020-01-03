package io.gridgo.socket.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Collection;
import java.util.function.Function;

import org.joo.promise4j.Promise;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import io.gridgo.connector.Responder;
import io.gridgo.connector.impl.SingleThreadSendingProducer;
import io.gridgo.connector.support.config.ConnectorContext;
import io.gridgo.connector.support.exceptions.FailureHandlerAware;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import io.gridgo.socket.exceptions.SendMessageException;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

public class DefaultSocketResponder extends SingleThreadSendingProducer
        implements FailureHandlerAware<DefaultSocketResponder>, Responder {

    private final ByteBuffer buffer;

    private final Socket socket;

    private Function<Throwable, Message> failureHandler;

    private final String uniqueIdentifier;

    @Getter
    private long totalSentBytes;

    @Getter
    private long totalSentMessages;

    private boolean useDirectBuffer = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;

    @Builder
    private DefaultSocketResponder(ConnectorContext context, //
            Socket socket, //
            int bufferSize, //
            int ringBufferSize, //
            boolean batchingEnabled, //
            int maxBatchSize, //
            String uniqueIdentifier, //
            Boolean useDirectBuffer) {

        super(context, 1024, new ThreadFactoryBuilder().build(), true, 100);

        if (useDirectBuffer != null)
            this.useDirectBuffer = useDirectBuffer.booleanValue();

        this.socket = socket;
        this.uniqueIdentifier = uniqueIdentifier;
        this.setFailureHandler(context.getExceptionHandler());

        this.buffer = (socket.forceUsingDirectBuffer() || this.useDirectBuffer) //
                ? ByteBuffer.allocateDirect(bufferSize)//
                : ByteBuffer.allocate(bufferSize);

    }

    @Override
    protected Message accumulateBatch(@NonNull Collection<Message> messages) {
        if (this.isBatchingEnabled()) {
            return SocketUtils.accumulateBatch(messages);
        }
        throw new IllegalStateException("Batching is disabled");
    }

    @Override
    public Promise<Message, Exception> call(Message request) {
        return Responder.super.call(request);
    }

    @Override
    protected void executeSendOnSingleThread(@NonNull Message message) throws Exception {
        Payload payload = message.getPayload();
        if (payload != null) {
            buffer.clear();
            payload.toBArray().writeBytes(buffer);
            buffer.flip();

            int sentBytes = this.socket.send(buffer);
            if (sentBytes == -1)
                if (this.failureHandler != null)
                    this.failureHandler.apply(new SendMessageException());

            totalSentBytes += sentBytes;
            totalSentMessages++;
        }
    }

    @Override
    protected String generateName() {
        return "responder." + this.uniqueIdentifier;
    }

    @Override
    public DefaultSocketResponder setFailureHandler(Function<Throwable, Message> failureHandler) {
        this.failureHandler = failureHandler;
        return this;
    }

}
