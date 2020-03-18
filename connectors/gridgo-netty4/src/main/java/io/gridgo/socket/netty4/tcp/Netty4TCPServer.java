package io.gridgo.socket.netty4.tcp;

import io.gridgo.bean.BElement;
import io.gridgo.socket.netty4.AbstractNetty4SocketServer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.socket.SocketChannel;

public class Netty4TCPServer extends AbstractNetty4SocketServer {

    @Override
    protected BElement handleIncomingMessage(String channelId, Object msg) throws Exception {
        return (BElement) msg;
    }

    @Override
    protected void onInitChannel(SocketChannel socketChannel) {
        Netty4RawChannelPreset.applyLengthPrepender(socketChannel);
        Netty4RawChannelPreset.applyBElementCodec(socketChannel, //
                getConfigs().getString("format", null), //
                getConfigs().getBoolean("nativeBytesEnabled", false));
    }

    @Override
    public final ChannelFuture send(String routingId, BElement data) {
        Channel channel = this.getChannel(routingId);
        if (channel != null) {
            if (data == null) {
                channel.close();
            } else {
                return channel.writeAndFlush(data).addListener(ChannelFutureListener.CLOSE);
            }
        }
        return null;
    }
}