package io.gridgo.socket;

import org.joo.promise4j.Promise;

import io.gridgo.connector.HasReceiver;
import io.gridgo.connector.Producer;
import io.gridgo.framework.support.Message;
import io.gridgo.socket.impl.DefaultSocketProducer;
import io.gridgo.socket.impl.DefaultSocketProducer.SocketProducerBuilder;

public interface SocketProducer extends Producer, HasReceiver {

    static SocketProducerBuilder builder() {
        return DefaultSocketProducer.builder();
    }

    @Override
    default Promise<Message, Exception> call(Message request) {
        throw new UnsupportedOperationException();
    }

    long getTotalSentBytes();

    long getTotalSentMessages();

    default boolean isCallSupported() {
        return false;
    }
}
