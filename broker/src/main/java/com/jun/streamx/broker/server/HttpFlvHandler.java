package com.jun.streamx.broker.server;

import com.jun.streamx.broker.javacv.FrameGrabAndRecordManager;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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

        // 抓取stream url
        String streamUrl = (String) ctx.channel().attr(AttributeKey.valueOf(ProtocolDispatchHandler.STREAM_URL_KEY)).get();

        // response
        var resp = new DefaultHttpResponse(HTTP_1_1, HttpResponseStatus.OK);
        resp.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "video/x-flv")
                .set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        ctx.writeAndFlush(resp);

        // start
        var manager = FrameGrabAndRecordManager.CACHE.computeIfAbsent(streamUrl, k -> new FrameGrabAndRecordManager(streamUrl, 10_000));
        manager.start().thenAccept(flvHeaders -> {
            ctx.writeAndFlush(Unpooled.wrappedBuffer(flvHeaders)).addListener(f -> {
                if (f.isSuccess()) {
                    manager.addReceiver(ctx.channel());
                } else {
                    log.warn("flv headers 发送失败", f.cause());
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
}
