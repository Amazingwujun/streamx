package com.jun.streamx.broker;

import com.jun.streamx.broker.codec.RtmpChunkCodec;
import com.jun.streamx.broker.codec.RtmpSimpleHandshakeHandler;
import com.jun.streamx.broker.handler.rtmp.MessageDispatchHandler;
import com.jun.streamx.broker.handler.rtmp.RtmpMessageHandler;
import com.jun.streamx.broker.net.ServerProperties;
import com.jun.streamx.broker.net.TcpServer;
import com.jun.streamx.commons.utils.ByteUtils;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.channel.socket.SocketChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class BrokerApplicationTests {

    @Autowired
    private RtmpMessageHandler rtmpMessageHandler;

    @Test
    void rtmp() throws InterruptedException {
        var sp = new ServerProperties();
        sp.setPort(1935);

        new TcpServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new RtmpSimpleHandshakeHandler())
                        .addLast(new RtmpChunkCodec())
                        .addLast(rtmpMessageHandler);
            }
        }, sp).start();

        TimeUnit.HOURS.sleep(1);
    }


    @Test
    void flv() throws InterruptedException {
        var sp = new ServerProperties();
        sp.setPort(1989);

        new TcpServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(new FlvDecoder());
            }
        }, sp).start();

        TimeUnit.HOURS.sleep(1);
    }
}
