package com.jun.streamx.broker;

import com.jun.streamx.net.ServerProperties;
import com.jun.streamx.net.TcpServer;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.TimeUnit;

@SpringBootTest
class BrokerApplicationTests {


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
