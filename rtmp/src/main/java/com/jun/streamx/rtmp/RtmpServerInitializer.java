package com.jun.streamx.rtmp;

import com.jun.streamx.rtmp.handler.RtmpMessageHandler;
import com.jun.streamx.rtmp.codec.RtmpChunkCodec;
import com.jun.streamx.rtmp.codec.RtmpSimpleHandshakeHandler;
import com.jun.streamx.rtmp.config.BizProperties;
import com.jun.streamx.net.ServerProperties;
import com.jun.streamx.net.TcpServer;
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
        this.port = bizProperties.getPort();
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
