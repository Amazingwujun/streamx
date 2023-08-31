package com.jun.streamx.broker.handler;

import com.jun.streamx.broker.javacv.FrameGrabAndRecordManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.AttributeKey;
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
        var streamUrl = (String) ctx.channel()
                .attr(AttributeKey.valueOf(ProtocolDispatchHandler.STREAM_URL_KEY)).get();

        // start
        var manager = FrameGrabAndRecordManager.CACHE.computeIfAbsent(streamUrl, k -> new FrameGrabAndRecordManager(streamUrl));
        manager.start().thenAccept(flvHeaders -> {
            ctx.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(flvHeaders))).addListener(f -> {
                if (f.isSuccess()) {
                    manager.addReceiver(ctx.channel());
                } else {
                    log.warn("flv headers 发送失败", f.cause());
                    ctx.close();
                }
            });
        }).exceptionally(t -> {
            log.error("Stream[{}] manager start 失败: {}", streamUrl, t.getMessage());

            // 移除 CACHE 中的 manager
            FrameGrabAndRecordManager.CACHE.remove(streamUrl);

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
}
