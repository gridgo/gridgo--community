package io.gridgo.socket.impl;

import static io.gridgo.socket.SocketConstants.BATCH_SIZE;
import static io.gridgo.socket.SocketConstants.IS_BATCH;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.function.Consumer;

import io.gridgo.bean.BArray;
import io.gridgo.bean.BObject;
import io.gridgo.framework.support.Message;
import io.gridgo.framework.support.Payload;
import io.gridgo.socket.Socket;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SocketUtils {

    public static Message accumulateBatch(@NonNull Collection<Message> messages) {
        int numMsgs = messages.size();
        if (numMsgs == 0)
            return null;

        if (numMsgs == 1)
            return messages.iterator().next();

        int i = 0;
        var payloadCask = new BArray[numMsgs];
        for (var msg : messages)
            payloadCask[i++] = msg.getPayload().toBArray();

        var body = BArray.wrap(Arrays.asList(payloadCask));
        var payload = Payload.of(body) //
                .addHeader(IS_BATCH, true) //
                .addHeader(BATCH_SIZE, numMsgs);

        return Message.of(payload);
    }

    private static void process(ByteBuffer buffer, //
            boolean skipTopicHeader, //
            Consumer<Message> receiver, //
            Consumer<Integer> recvByteCounter, //
            Consumer<Integer> recvMsgCounter, //
            Consumer<Throwable> exceptionHandler, int rc) {

        recvByteCounter.accept(rc);

        try {
            buffer.flip();
            if (skipTopicHeader) {
                byte b = buffer.get();
                while (b != 0) {
                    b = buffer.get();
                }
            }

            var message = Message.parse(buffer);
            var headers = message.headers();

            if (headers != null && headers.getBoolean(IS_BATCH, false)) {
                processBatch(receiver, recvMsgCounter, message, headers);
            } else {
                recvMsgCounter.accept(1);
                receiver.accept(message);
            }

        } catch (Exception e) {
            if (log.isTraceEnabled())
                log.trace("Error while parse buffer to message", e);

            if (exceptionHandler != null)
                exceptionHandler.accept(e);
        }
    }

    private static void processBatch(Consumer<Message> receiver, //
            Consumer<Integer> recvMsgCounter, //
            Message message, //
            BObject headers) {

        var batch = message.body().asArray();
        recvMsgCounter.accept(headers.getInteger(BATCH_SIZE, batch.size()));

        for (var msg : batch)
            receiver.accept(Message.parse(msg));
    }

    public static void startPolling( //
            Socket socket, //
            ByteBuffer buffer, //
            boolean skipTopicHeader, //
            Consumer<Message> receiver, //
            Consumer<Integer> recvByteCounter, //
            Consumer<Integer> recvMsgCounter, //
            Consumer<Throwable> exceptionHandler) {

        while (!Thread.currentThread().isInterrupted()) {
            buffer.clear();
            int rc = socket.receive(buffer);

            if (rc < 0) {
                if (Thread.currentThread().isInterrupted())
                    break;
            } else {
                process(buffer, skipTopicHeader, receiver, recvByteCounter, recvMsgCounter, exceptionHandler, rc);
            }
        }
    }
}
