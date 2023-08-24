package com.jun.streamx.broker.server;

import com.jun.streamx.broker.config.BizProperties;
import com.jun.streamx.broker.constants.ProtocolEnum;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * 协议选择器
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class ProtocolDispatchHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final String PROTOCOL = "protocol";
    public static final String STREAM_URL_KEY = "stream-url";
    private final HttpFlvHandler httpFlvHandler;
    private final WebSocketFlvHandler webSocketFlvHandler;
    private final String webSocketPath;

    public ProtocolDispatchHandler(BizProperties bizProperties,
                                   HttpFlvHandler httpFlvHandler,
                                   WebSocketFlvHandler webSocketFlvHandler) {
        this.webSocketPath = bizProperties.getWebSocketPath();
        this.httpFlvHandler = httpFlvHandler;
        this.webSocketFlvHandler = webSocketFlvHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        var headers = msg.headers();

        // uri 检查
        var uri = URI.create(msg.uri());
        String query = uri.getQuery();
        if (query == null || !query.contains("=")) {
            sendError(ctx, "uri query string error");
            return;
        }
        String streamUrl = query.split("=")[1];
        if (!(streamUrl.startsWith("rtsp") || streamUrl.startsWith("rtmp"))) {
            var errMsg = String.format("非法的 stream url [%s], 仅支持 rtsp/rtmp", streamUrl);
            log.error(errMsg);
            sendError(ctx, errMsg);
            return;
        }

        // 关联 stream url 到 channel
        ctx.channel().attr(AttributeKey.valueOf(STREAM_URL_KEY)).set(streamUrl);

        // websocket 与 http 协议处理
        if (isWebsocketUpgrade(headers)) {
            // path check
            var path = uri.getPath();
            if (!webSocketPath.equals(path)) {
                log.warn("非法的 WebSocket path: [{}]", path);
                ctx.close();
                return;
            }

            // 协议内容置入
            ctx.channel().attr(AttributeKey.valueOf(PROTOCOL)).set(ProtocolEnum.ws);

            // ctx 切换
            var p = ctx.pipeline();
            p.remove(ChunkedWriteHandler.class);
            p.remove(this);
            p.addLast(new WebSocketServerCompressionHandler());
            p.addLast(new WebSocketServerProtocolHandler(webSocketPath, null, true));
            p.addLast(webSocketFlvHandler);

            // 跳过 websocket uri 检查
            msg.setUri(path);

            // channel active 及 channel
            var preCtx = p.context(CorsHandler.class);
            preCtx.fireChannelRead(msg.retain());
            preCtx.fireChannelActive();
        } else {
            // 协议内容置入
            ctx.channel().attr(AttributeKey.valueOf(PROTOCOL)).set(ProtocolEnum.http);

            var p = ctx.pipeline();
            p.remove(this);
            p.addLast(httpFlvHandler);

            p.fireChannelRead(msg.retain());
            p.fireChannelActive();
        }
    }

    boolean isWebsocketUpgrade(HttpHeaders headers) {
        //this contains check does not allocate an iterator, and most requests are not upgrades
        //so we do the contains check first before checking for specific values
        return headers.contains(HttpHeaderNames.UPGRADE) &&
                headers.containsValue(HttpHeaderNames.CONNECTION, HttpHeaderValues.UPGRADE, true) &&
                headers.contains(HttpHeaderNames.UPGRADE, HttpHeaderValues.WEBSOCKET, true);
    }

    private void sendError(ChannelHandlerContext ctx, String errMsg) {
        FullHttpResponse response = new DefaultFullHttpResponse(
                HTTP_1_1, HttpResponseStatus.BAD_REQUEST, Unpooled.copiedBuffer(errMsg, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
