package com.zhw.skynet.core;

import com.zhw.skynet.common.Constants;
import com.zhw.skynet.core.protocol.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollDomainSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.channel.unix.DomainSocketAddress;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;

import java.io.File;
import java.net.InetSocketAddress;
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
            if (msg instanceof RequestMessage) {
                try {
                    executor.execute(new DoInvokeTask((RequestMessage) msg));
                    return;
                } catch (Throwable e) {
                    log.warn("error submit to executor", e);
                }
            }
            super.channelRead(ctx, msg);
        }
    }

    private class DoInvokeTask implements Runnable {
        private RequestMessage reqMsg;

        public DoInvokeTask(RequestMessage reqMsg) {
            this.reqMsg = reqMsg;
        }

        @Override
        public void run() {
            ServiceInvoker invoker = invokerMap.get(reqMsg.getService());
            Response resp = new Response();
            resp.setReqId(reqMsg.getRequestId());
            resp.setClientId(reqMsg.getClientId());
            resp.setServerId(reqMsg.getServerId());
            ServiceMeta meta = null;
            if (invoker == null) {
                reqMsg.releaseBodyBuf();
                resp.setCode(Constants.CODE_SERVER_NO_SERVICE);
                resp.setBody("no such service " + reqMsg.getService());
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
                buf = encoder.encode(resp, meta);
            } catch (Throwable e) {
                log.error(e);
                // 有可能是result编码失败
                if (resp.getCode() == 0) {
                    resp.setCode(errorToCode(e));
                    resp.setErr(e);
                    resp.setBody(null);
                    try {
                        buf = encoder.encode(resp, meta);
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
        if (err instanceof DecodeException) {
            return Constants.CODE_SERVER_DECODE_ERROR;
        } else if (err instanceof ValidateException) {
            return Constants.CODE_SERVER_PARAM_VALID_ERROR;
        } else {
            log.error(err);
            return Constants.CODE_SERVER_RUNTIME_ERROR;
        }
    }

    private Request convertToRequest(RequestMessage msg, ServiceMeta meta) throws DecodeException {
        ByteBuf buf = msg.getBodyBuf();
        Object obj = null;
        if (msg.getServiceLen() > 0 && buf != null) {
            try {
                obj = meta.getRequestMapper().read(buf, 0);
            } finally {
                msg.releaseBodyBuf();
            }
        }
        return new Request(msg.getRequestId(), meta, obj);
    }

    private Encoder<Response> encoder = new ResponseEncoder();
    private Bootstrap bootstrap;
    private Channel channel;
    private Map<String, ServiceInvoker> invokerMap = new HashMap<>();
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
                ch.pipeline().addLast(new ServerShakeHandsHandler())
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
                ch.pipeline().addLast(new ServerShakeHandsHandler())
                        .addLast("ResponseReceiveHandler", new ReqMsgReceiveHandler());
            }
        });
        this.executor = executor;
    }


    public void addInvoker(ServiceInvoker invoker) {
        if (channel != null) {
            throw new IllegalStateException("connected");
        }
        String name = invoker.getServiceMeta().getServiceName();
        ServiceInvoker old = invokerMap.put(name, invoker);
        if (old != null) {
            throw new IllegalStateException(String.format("%s added with %s", name, invoker));
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

        List<ShakeHandsReq.ServiceCount> list = handsReq.waitResponse(30000);
        if (list == null) {
            channel.close().awaitUninterruptibly();
            throw new ShakeHandsException("timeout");
        }

        if (log.isDebugEnabled()) {
            for (ShakeHandsReq.ServiceCount count : list) {
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
