package com.jun.streamx.rtmp.handler;

import com.jun.streamx.commons.constants.Protocol;
import com.jun.streamx.rtmp.entity.RtmpSession;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.group.ChannelGroup;
import io.netty.util.AttributeKey;

import java.util.Map;

/**
 * 该抽象类提供一系列公共方法
 *
 * @author Jun
 * @since 1.0.0
 */
public abstract class AbstractMessageHandler implements MessageHandler {

    protected final Map<String, ChannelGroup> subscribers = RtmpMessageHandler.SUBSCRIBERS;
    protected final Map<String, Channel> publishers = RtmpMessageHandler.PUBLISHERS;

    public RtmpSession getSession(ChannelHandlerContext ctx) {
        return (RtmpSession) ctx.channel().attr(AttributeKey.valueOf(RtmpSession.KEY)).get();
    }

    public void saveSession(ChannelHandlerContext ctx, RtmpSession session) {
        ctx.channel().attr(AttributeKey.valueOf(RtmpSession.KEY)).set(session);
    }

    public RtmpSession newSession(ChannelHandlerContext ctx) {
        var session = new RtmpSession();
        saveSession(ctx, session);
        return session;
    }

    public RtmpSession getSession(Channel channel) {
        return (RtmpSession) channel.attr(AttributeKey.valueOf(RtmpSession.KEY)).get();
    }

    /**
     * 获取当前 channel 的 protocol
     *
     * @param channel {@link Channel}
     * @return ProtocolEnum
     */
    public Protocol protocol(Channel channel) {
        return (Protocol) channel.attr(AttributeKey.valueOf(Protocol.key)).get();
    }
}
