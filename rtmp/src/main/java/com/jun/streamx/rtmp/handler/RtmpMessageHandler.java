package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.entity.RtmpMessage;
import com.jun.streamx.rtmp.entity.RtmpSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rtmp message handler
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class RtmpMessageHandler extends SimpleChannelInboundHandler<RtmpMessage> {
    //@formatter:off

    /** k: app + stream name, v: 订阅客户端列表 */
    public static final Map<String, ChannelGroup> SUBSCRIBERS = new ConcurrentHashMap<>();
    /** 推流 channel */
    public static final Map<String, Channel> PUBLISHERS = new ConcurrentHashMap<>();

    //@formatter:on

    private final MessageDispatchHandler messageDispatchHandler;

    public RtmpMessageHandler(MessageDispatchHandler messageDispatchHandler) {
        this.messageDispatchHandler = messageDispatchHandler;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        var socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        var host = socketAddress.getAddress().getHostAddress();
        var port = socketAddress.getPort();

        log.info("[{}:{}] 连接激活", host, port);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RtmpMessage msg) {
        messageDispatchHandler.process(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channel[{}] inactive", ctx.channel().id());

        // 移除 channel
        var session = (RtmpSession) ctx.channel().attr(AttributeKey.valueOf(RtmpSession.KEY)).get();
        if (session != null) {
            if (RtmpSession.Type.publisher == session.getType()) {
                PUBLISHERS.remove(session.streamKey());

                // 变更 session 状态
                session.complete(RtmpSession.State.inactive);

                // 释放 session 里的 keyframe
                session.release();
            }

            var socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();
            var host = socketAddress.getAddress().getHostAddress();
            var port = socketAddress.getPort();

            log.info("{}[{}:{}] 连接关闭", session.getType(), host, port);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }
}
