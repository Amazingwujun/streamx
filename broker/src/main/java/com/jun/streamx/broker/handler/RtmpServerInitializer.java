package com.jun.streamx.broker.handler;

import com.jun.streamx.broker.codec.RtmpChunkCodec;
import com.jun.streamx.broker.codec.RtmpSimpleHandshakeHandler;
import com.jun.streamx.broker.config.BizProperties;
import com.jun.streamx.broker.net.ServerProperties;
import com.jun.streamx.broker.net.TcpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * rtmp 服务器
 *
 * @author Jun
 * @since 1.0.0
 */
@Component
public class RtmpServerInitializer {

    private final RtmpMessageHandler rtmpMessageHandler;
    private final int port;

    public RtmpServerInitializer(BizProperties bizProperties, RtmpMessageHandler rtmpMessageHandler) {
        this.port = bizProperties.getRtmpPort();
        this.rtmpMessageHandler = rtmpMessageHandler;
    }

    @PostConstruct
    private void launch() throws InterruptedException {
        var sp = new ServerProperties();
        sp.setPort(port);
        new TcpServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new RtmpSimpleHandshakeHandler())
                        .addLast(new RtmpChunkCodec())
                        .addLast(rtmpMessageHandler);
            }
        }, sp).start();
    }
}
