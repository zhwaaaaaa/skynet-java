package com.zhw.skynet.core.server;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.common.RpcException;
import com.zhw.skynet.core.*;
import com.zhw.skynet.core.protocol.Codec;
import com.zhw.skynet.core.protocol.CodecException;
import com.zhw.skynet.core.protocol.RequestMessage;
import com.zhw.skynet.core.sh.ShakeHandsException;
import com.zhw.skynet.core.sh.ShakeHandsHandler;
import com.zhw.skynet.core.sh.ShakeHandsReq;
import com.zhw.skynet.core.sh.ShakeHandsResp;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class Server implements EndPoint {
    private static final InternalLogger log = InternalLoggerFactory.getInstance(Server.class);

    private final EventLoopGroup GROUP = new NioEventLoopGroup(1);

    private class ReqMsgReceiveHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (msg instanceof ByteBuf) {
                try {
                    executor.execute(new DoInvokeTask((ByteBuf) msg));
                    return;
                } catch (Throwable e) {
                    log.warn("error submit to executor", e);
                }
            }
            super.channelRead(ctx, msg);
        }
    }

    private class DoInvokeTask implements Runnable {
        private ByteBuf in;

        public DoInvokeTask(ByteBuf in) {
            this.in = in;
        }

        @Override
        public void run() {

            RequestMessage reqMsg = codec.decodeRequest(in);

            ServiceMetaKey serviceMetaKey = ServiceMetaKey.of(reqMsg);
            ServiceInvoker invoker = invokerMap.get(serviceMetaKey);
            Response resp = new Response();
            resp.setReqId(reqMsg.getRequestId());
            resp.setClientId(reqMsg.getClientId());
            resp.setServerId(reqMsg.getServerId());
            ServiceMeta meta = null;
            if (invoker == null) {
                reqMsg.releaseBodyBuf();
                resp.setCode(Constants.CODE_SERVER_NO_SERVICE);
                resp.setBody(new RpcException("no such service " + serviceMetaKey, null, false, false));
            } else {
                try {
                    meta = invoker.getServiceMeta();
                    Request request = convertToRequest(reqMsg, meta);
                    Object result = invoker.invoke(request.getBody());
                    resp.setBody(result);
                } catch (Throwable e) {
                    // TODO
                    resp.setCode(errorToCode(e));
                    resp.setErr(e);
                }
            }
            ByteBuf buf = null;
            try {
                buf = codec.encode(resp, meta);
            } catch (Throwable e) {
                log.error(e);
                // 有可能是result编码失败
                if (resp.getCode() == 0) {
                    resp.setCode(errorToCode(e));
                    resp.setErr(e);
                    resp.setBody(null);
                    try {
                        buf = codec.encode(resp, meta);
                    } catch (Throwable err) {
                        log.error("error encode {}", resp, meta);
                    }
                }
            }
            if (buf != null) {
                channel.writeAndFlush(buf);
            }
        }
    }

    public static int errorToCode(Throwable err) {
        if (err instanceof CodecException) {
            return Constants.CODE_SERVER_DECODE_ERROR;
        } else if (err instanceof ValidateException) {
            return Constants.CODE_SERVER_PARAM_VALID_ERROR;
        } else {
            log.error(err);
            return Constants.CODE_SERVER_RUNTIME_ERROR;
        }
    }

    private Request convertToRequest(RequestMessage msg, ServiceMeta meta) throws CodecException {
        Object body = codec.decodeBody(msg, meta);
        return new Request(msg.getRequestId(), meta, body);
    }

    private Codec<Response, RequestMessage> codec = new ServerCodec(ByteBufAllocator.DEFAULT);
    private Bootstrap bootstrap;
    private Channel channel;
    private Map<ServiceMetaKey, ServiceInvoker> invokerMap = new HashMap<>();
    private Executor executor;


    public Server(String host, int port, Executor executor) {
        bootstrap = new Bootstrap();
        bootstrap.group(GROUP);
        bootstrap.remoteAddress(new InetSocketAddress(host, port));
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline().addLast("msgSplitHandler", new LengthFieldBasedFrameDecoder(
                        ByteOrder.LITTLE_ENDIAN,
                        Constants.MAX_MSG_LEN,
                        1,
                        4,
                        0,
                        0,
                        true
                ))
                        .addLast(ShakeHandsHandler.forProvider())
                        .addLast("ResponseReceiveHandler", new ReqMsgReceiveHandler());
            }
        });
        this.executor = executor;
    }

    public Server(File unixSockFile, Executor executor) {
        if (!unixSockFile.exists()) {
            throw new IllegalStateException("socket file not exists");
        }
        bootstrap = new Bootstrap();
        bootstrap.group(GROUP);
        bootstrap.remoteAddress(new DomainSocketAddress(unixSockFile));
        bootstrap.channel(EpollDomainSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<NioSocketChannel>() {
            protected void initChannel(NioSocketChannel ch) throws Exception {
                ch.pipeline()
                        .addLast("msgSplitHandler", new LengthFieldBasedFrameDecoder(
                                ByteOrder.LITTLE_ENDIAN,
                                Constants.MAX_MSG_LEN,
                                1,
                                4,
                                0,
                                0,
                                true
                        )).addLast(ShakeHandsHandler.forProvider())
                        .addLast("ResponseReceiveHandler", new ReqMsgReceiveHandler());
            }
        });
        this.executor = executor;
    }


    public void addInvoker(ServiceInvoker invoker) {
        if (channel != null) {
            throw new IllegalStateException("connected");
        }
        ServiceMeta meta = invoker.getServiceMeta();
        ServiceMetaKey of = ServiceMetaKey.of(meta);
        ServiceInvoker old = invokerMap.put(of, invoker);
        if (old != null) {
            throw new IllegalStateException(String.format("%s added with %s", of, invoker));
        }
    }

    public void start() throws Throwable {
        List<ServiceMeta> metas = invokerMap.values().stream()
                .map(ServiceInvoker::getServiceMeta).collect(Collectors.toList());
        EndPoint.validServiceMetas(metas);
        ChannelFuture f = bootstrap.connect().awaitUninterruptibly();
        if (!f.isSuccess()) {
            throw f.cause();
        }

        Channel channel = f.channel();
        ShakeHandsReq handsReq = new ShakeHandsReq(metas);
        channel.writeAndFlush(handsReq).awaitUninterruptibly();

        ShakeHandsResp resp = handsReq.waitResponse(30000);
        if (resp == null) {
            channel.close().awaitUninterruptibly();
            throw new ShakeHandsException("timeout");
        }

        if (log.isDebugEnabled()) {
            for (ShakeHandsResp.ServiceCount count : resp.getCounts()) {
                log.debug("registered service {}", count);
            }
        }
        this.channel = channel;
    }

    @Override
    public void close() {
        Channel ch = this.channel;
        if (ch != null) {
            ch.close().awaitUninterruptibly();
            this.channel = null;
        }
    }
}
