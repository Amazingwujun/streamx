package com.jun.streamx.broker;

import com.jun.streamx.broker.codec.RtmpChunkDecoder;
import com.jun.streamx.broker.codec.RtmpSimpleHandshakeHandler;
import com.jun.streamx.broker.handler.RtmpMessageHandler;
import com.jun.streamx.broker.net.ServerProperties;
import com.jun.streamx.broker.net.TcpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class BrokerApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        var sp = new ServerProperties();
        sp.setPort(1935);

        new TcpServer(new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline()
                        .addLast(new RtmpSimpleHandshakeHandler())
                        .addLast(new RtmpChunkDecoder())
                        .addLast(new RtmpMessageHandler());
            }
        }, sp).start();

        TimeUnit.HOURS.sleep(1);

        var s = "c3 ";
    }


}
