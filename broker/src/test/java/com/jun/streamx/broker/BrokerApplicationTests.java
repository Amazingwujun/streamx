package com.jun.streamx.broker;

import com.jun.streamx.broker.codec.RtmpChunkCodec;
import com.jun.streamx.broker.codec.RtmpSimpleHandshakeHandler;
import com.jun.streamx.broker.handler.RtmpMessageHandler;
import com.jun.streamx.broker.net.ServerProperties;
import com.jun.streamx.broker.net.TcpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

//@SpringBootTest
class BrokerApplicationTests {

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
                        .addLast(new RtmpMessageHandler());
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
