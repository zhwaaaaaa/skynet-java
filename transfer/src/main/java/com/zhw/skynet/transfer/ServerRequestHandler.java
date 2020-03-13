package com.zhw.skynet.transfer;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

public class ServerRequestHandler extends ValidHeaderHandler implements RemoteEndPoint {
    private final int id;
    private final ProviderServiceManager providerServiceManager;
    private Channel ch;

    public ServerRequestHandler(int id, ProviderServiceManager providerServiceManager) {
        this.id = id;
        this.providerServiceManager = providerServiceManager;
    }

    @Override
    protected boolean handleReq(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        return super.handleReq(ctx, in);
    }

    @Override
    public boolean send(ByteBuf buf) {
        if (ch == null) {
            return false;
        }
        ch.writeAndFlush(buf);
        return true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ch = ctx.channel();
        providerServiceManager.putResponseReceiver(id, this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.ch = null;
        RemoteEndPoint endPoint = providerServiceManager.removeResponseReceiver(id);
        assert endPoint == this;
    }

    @Override
    public List<ServiceDesc> relateServices() {
        return null;
    }

    @Override
    public void close() {
        if (ch != null) {
            ch.close();
        }
    }
}
