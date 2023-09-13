package com.jun.streamx.rtmp.http;

import com.jun.streamx.rtmp.entity.RtmpSession;
import com.jun.streamx.rtmp.entity.StreamInfo;
import com.jun.streamx.rtmp.handler.RtmpMessageHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_0;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * http, websocket 协议 flv 请求处理器
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Component
@ChannelHandler.Sharable
public class HttpFlvHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    protected final Map<String, ChannelGroup> subscribers = RtmpMessageHandler.SUBSCRIBERS;
    protected final Map<String, Channel> publishers = RtmpMessageHandler.PUBLISHERS;
    private FullHttpRequest request;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel[{}] active", ctx.channel().id());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
        this.request = request;
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }

        // stream 获取
        var stream = (StreamInfo) ctx.channel()
                .attr(AttributeKey.valueOf(ProtocolDispatchHandler.STREAM_INFO_KEY)).get();
        var streamKey = stream.key();

        // response
        var resp = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        resp.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "video/x-flv")
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(resp);

        // 找到对应的 publisher
        var publisherChannel = publishers.get(streamKey);
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

                    ctx.writeAndFlush(publisherSession.getFlvHeaders().copy())
                            .addListener(f -> {
                                if (f.isSuccess()) {
                                    subscribers.compute(streamKey, (k, v) -> {
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
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channel[{}] inactive", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        sendAndCleanupConnection(ctx, response);
    }

    /**
     * If Keep-Alive is disabled, attaches "Connection: close" header to the response
     * and closes the connection after the response being sent.
     */
    private void sendAndCleanupConnection(ChannelHandlerContext ctx, FullHttpResponse response) {
        final FullHttpRequest request = this.request;
        final boolean keepAlive = HttpUtil.isKeepAlive(request);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        if (!keepAlive) {
            // We're going to close the connection as soon as the response is sent,
            // so we should also make it clear for the client.
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        } else if (request.protocolVersion().equals(HTTP_1_0)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }

        ChannelFuture flushPromise = ctx.writeAndFlush(response);

        if (!keepAlive) {
            // Close the connection as soon as the response is sent.
            flushPromise.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public RtmpSession getSession(Channel channel) {
        return (RtmpSession) channel.attr(AttributeKey.valueOf(RtmpSession.KEY)).get();
    }
}
