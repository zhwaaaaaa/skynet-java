package com.zhw.skynet.transfer;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.ServiceCount;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ClientRequestHandler extends ValidHeaderHandler implements RemoteEndPoint {
    private Channel ch;
    private ShakeRequest req;
    private Set<String> services;

    private final RemoteServiceManager remoteServiceManager;
    private final int id;

    public ClientRequestHandler(RemoteServiceManager remoteServiceManager, int id) {
        this.remoteServiceManager = remoteServiceManager;
        this.id = id;
    }

    @Override
    protected boolean handleShakeHands(int code, ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ShakeRequest request = ShakeHandsUtil.decodeShakeHandsReq(in);
        if (request == null) {
            ctx.close();
            return false;
        }
        services = request.getDesc().stream().map(ServiceDesc::getServiceName).collect(Collectors.toSet());
        req = request;
        List<ServiceCount> counts = request.getDesc().stream()
                .map(x -> new ServiceCount(x.getServiceName(), remoteServiceManager.addChannelService(x, id)))
                .collect(Collectors.toList());
        ctx.writeAndFlush(ShakeHandsUtil.encodeServiceCounts(counts, ctx.alloc().buffer()));
        return false;
    }

    @Override
    protected boolean handleReq(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        // request [1][4len][4][4][4][1][service][1][data]
        // response [1][4len][4][4][4][1code][1type][data]
        int servLen = in.getUnsignedByte(in.readerIndex() + 17);
        String service = in.toString(in.readerIndex() + 18, servLen, Constants.UTF8);
        RemoteEndPointGroup group = remoteServiceManager.get(service);
        if (group == null) {
            // write no service
            ByteBuf buf = ctx.alloc().buffer(19);
            buf.writeByte(Constants.MSG_TYPE_TRANSFER_RESP);
            buf.writeIntLE(14);// 长度 14个字节。这个包应该是19个字节
            buf.writeIntLE(in.getIntLE(in.readerIndex() + 5));
            buf.writeZero(8);
            buf.writeByte(Constants.CODE_SERVER_NO_SERVICE);
            buf.writeZero(1);
            ctx.writeAndFlush(buf);
            return false;
        }

        // write channelId to msg
        in.setIntLE(in.readerIndex() + 9, id);
        if (!group.send(in)) {
            //TODO send error
        }
        return true;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        this.ch = ctx.channel();
        remoteServiceManager.putResponseReceiver(id, this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        this.ch = null;
        RemoteEndPoint endPoint = remoteServiceManager.removeResponseReceiver(id);
        assert endPoint == this;

        if (req != null) {
            for (ServiceDesc desc : req.getDesc()) {
                remoteServiceManager.removeChannelService(desc, id);
            }
        }
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
    public Set<String> relateServices() {
        return services;
    }

    @Override
    public void close() {
        if (ch != null) {
            ch.close();
        }
    }
}