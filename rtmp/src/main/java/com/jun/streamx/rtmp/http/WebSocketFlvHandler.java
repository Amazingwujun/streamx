package com.jun.streamx.rtmp.http;

import com.jun.streamx.rtmp.entity.RtmpSession;
import com.jun.streamx.rtmp.entity.StreamInfo;
import com.jun.streamx.rtmp.handler.RtmpMessageHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 基于 websocket 协议的 flv 处理器
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class WebSocketFlvHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel[{}] active", ctx.channel().id());

        // streamUrl 获取
        var stream = (StreamInfo) ctx.channel()
                .attr(AttributeKey.valueOf(ProtocolDispatchHandler.STREAM_INFO_KEY)).get();
        var streamKey = stream.key();

        // 找到对应的 publisher
        var publisherChannel = RtmpMessageHandler.PUBLISHERS.get(streamKey);
        if (publisherChannel == null) {
            log.warn("Stream[{}] 没有 publisher", streamKey);
            ctx.close();
            return;
        }
        final var publisherSession = getSession(publisherChannel);
        publisherSession.thenAccept(state -> {
                    if (state != RtmpSession.State.complete) {
                        log.warn("publisher state is {}", state);
                        return;
                    }

                    ctx.writeAndFlush(new BinaryWebSocketFrame(publisherSession.getFlvHeaders().copy()))
                            .addListener(f -> {
                                if (f.isSuccess()) {
                                    RtmpMessageHandler.SUBSCRIBERS.compute(streamKey, (k, v) -> {
                                        if (v == null) {
                                            v = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
                                        }

                                        v.add(ctx.channel());
                                        return v;
                                    });
                                } else {
                                    log.error("flv headers 发送失败" + f.cause().getMessage(), f.cause());
                                    ctx.close();
                                }
                            });
                })
                .exceptionally(throwable -> {
                    log.error("publisher session completeFailed: " + throwable.getMessage(), throwable);

                    // 关闭当前连接
                    ctx.close();

                    return null;
                });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) {
        // do nothing
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channel[{}] inactive", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    public RtmpSession getSession(Channel channel) {
        return (RtmpSession) channel.attr(AttributeKey.valueOf(RtmpSession.KEY)).get();
    }
}
