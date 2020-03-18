package io.gridgo.socket.netty4.tcp;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.AbstractNetty4SocketClient;
import io.netty.channel.socket.SocketChannel;

public class Netty4TCPClient extends AbstractNetty4SocketClient {

    @Override
    protected BElement handleIncomingMessage(Object msg) throws Exception {
        return (BElement) msg;
    }

    @Override
    protected void onInitChannel(SocketChannel socketChannel) {
        Netty4RawChannelPreset.applyLengthPrepender(socketChannel);
        Netty4RawChannelPreset.applyBElementCodec(socketChannel, //
                getConfigs().getString("format", null), //
                getConfigs().getBoolean("nativeBytesEnabled", false));
    }
}
