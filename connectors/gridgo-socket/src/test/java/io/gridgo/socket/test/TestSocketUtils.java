package io.gridgo.socket.test;

import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.Test;

import io.gridgo.socket.impl.SocketUtils;
import io.gridgo.socket.test.support.TestSocket;
import io.gridgo.utils.ThreadUtils;

public class TestSocketUtils {

    @Test
    public void testSocketUtilsPolling() {
        var socket = new TestSocket();
        var buffer = ByteBuffer.allocate(1024);
        var interupted = new AtomicBoolean(false);
        var thread = new Thread(() -> {
            SocketUtils.startPolling(socket, buffer, true, msg -> {
            }, recvBytes -> {
            }, recvMsgs -> {
            }, null);
            interupted.set(Thread.currentThread().isInterrupted());
        });
        thread.start();
        thread.interrupt();
        ThreadUtils.sleep(100);
        assertTrue(interupted.get());
    }
}
