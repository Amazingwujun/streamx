package com.jun.streamx.broker.server;

import com.jun.streamx.broker.config.BizProperties;
import com.jun.streamx.broker.net.ServerProperties;
import com.jun.streamx.broker.net.TcpServer;
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

    public HttpServerInitializer(BizProperties bp,
                                 HttpFlvHandler httpFlvHandler,
                                 WebSocketFlvHandler webSocketFlvHandler) {
        this.port = bp.getPort();
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
                        .addLast(new ProtocolDispatchHandler(httpFlvHandler, webSocketFlvHandler));
            }
        }, serverProperties);
        httpServer.start();
    }

}
