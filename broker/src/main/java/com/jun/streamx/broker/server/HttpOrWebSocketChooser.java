package com.jun.streamx.broker.server;

import com.jun.streamx.broker.constants.ProtocolEnum;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.AttributeKey;

import java.net.URI;

/**
 * 协议选择器
 *
 * @author Jun
 * @since 1.0.0
 */
public class HttpOrWebSocketChooser extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final String PROTOCOL = "protocol";
    public static final String STREAM_URL_KEY = "stream-url";
    private final HttpFlvHandler httpFlvHandler;
    private final WebSocketFlvHandler webSocketFlvHandler;

    public HttpOrWebSocketChooser(HttpFlvHandler httpFlvHandler, WebSocketFlvHandler webSocketFlvHandler) {
        this.httpFlvHandler = httpFlvHandler;
        this.webSocketFlvHandler = webSocketFlvHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) {
        var headers = msg.headers();
        if (isWebsocketUpgrade(headers)) {
            // 协议内容置入
            ctx.channel().attr(AttributeKey.valueOf(PROTOCOL)).set(ProtocolEnum.ws);

            // 抓取 stream url
            String uri = msg.uri();
            String query = URI.create(uri).getQuery();
            String streamUrl = query.split("=")[1];
            ctx.channel().attr(AttributeKey.valueOf(STREAM_URL_KEY)).set(streamUrl);

            // ctx 切换
            var p = ctx.pipeline();
            p.remove(ChunkedWriteHandler.class);
            p.remove(this);
            p.addLast(new WebSocketServerCompressionHandler());
            p.addLast(new WebSocketServerProtocolHandler("/live", null, true));
            p.addLast(webSocketFlvHandler);

            msg.setUri(uri.split("\\?")[0]);

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
}
