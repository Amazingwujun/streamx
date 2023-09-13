package com.jun.streamx.rtmp;

import com.jun.streamx.net.ServerProperties;
import com.jun.streamx.net.TcpServer;
import com.jun.streamx.rtmp.config.BizProperties;
import com.jun.streamx.rtmp.http.HttpFlvHandler;
import com.jun.streamx.rtmp.http.ProtocolDispatchHandler;
import com.jun.streamx.rtmp.http.WebSocketFlvHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.cors.CorsConfigBuilder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * http-flv 服务器
 *
 * @author Jun
 * @since 1.0.0
 */
@Component
public class HttpServerInitializer {

    private final HttpFlvHandler httpFlvHandler;
    private final WebSocketFlvHandler webSocketFlvHandler;
    private final int port;
    private final BizProperties bizProperties;

    public HttpServerInitializer(BizProperties bizProperties,
                                 HttpFlvHandler httpFlvHandler,
                                 WebSocketFlvHandler webSocketFlvHandler) {
        this.port = bizProperties.getHttpFlv().getPort();
        this.bizProperties = bizProperties;
        this.httpFlvHandler = httpFlvHandler;
        this.webSocketFlvHandler = webSocketFlvHandler;
    }

    @PostConstruct
    private void launch() throws InterruptedException {
        var serverProperties = new ServerProperties();
        serverProperties.setPort(port);
        var httpServer = new TcpServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) {
                var corsConfig = CorsConfigBuilder.forAnyOrigin()
                        .allowNullOrigin()
                        .allowCredentials()
                        .build();
                var p = socketChannel.pipeline();
                p
                        .addLast(new HttpServerCodec())
                        .addLast(new ChunkedWriteHandler())
                        .addLast(new HttpObjectAggregator(64 * 1024))
                        .addLast(new CorsHandler(corsConfig))
                        .addLast(new ProtocolDispatchHandler(bizProperties, httpFlvHandler, webSocketFlvHandler));
            }
        }, serverProperties);
        httpServer.start();
    }

}
