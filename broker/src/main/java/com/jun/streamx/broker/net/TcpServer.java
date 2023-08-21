package com.jun.streamx.broker.net;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.springframework.util.Assert;

/**
 * tcp 服务器
 *
 * @author Jun
 * @since 1.0.0
 */
public class TcpServer {

    private final EventLoopGroup boss, work;
    private final ChannelInitializer<SocketChannel> channelInitializer;
    private final ServerProperties config;


    /**
     * 使用新的 {@link EventLoopGroup} 创建 server
     *
     * @param channelInitializer {@link ChannelInitializer}
     * @param properties         {@link ServerProperties}
     */
    public TcpServer(ChannelInitializer<SocketChannel> channelInitializer, ServerProperties properties) {
        Assert.notNull(channelInitializer, "channelInitializer can't be null");
        Assert.notNull(properties, "ServerConfig can't be null");
        if (Epoll.isAvailable()) {
            boss = new EpollEventLoopGroup(1);
            work = new EpollEventLoopGroup();
        } else {
            boss = new NioEventLoopGroup(1);
            work = new NioEventLoopGroup();
        }
        this.channelInitializer = channelInitializer;
        this.config = properties;
    }

    /**
     * 使用指定的 {@link EventLoopGroup} 创建 server
     *
     * @param channelInitializer {@link ChannelInitializer}
     * @param properties         {@link ServerProperties}
     * @param boss               {@link EventLoopGroup}
     * @param work               {@link EventLoopGroup}
     */
    public TcpServer(ChannelInitializer<SocketChannel> channelInitializer, ServerProperties properties, EventLoopGroup boss, EventLoopGroup work) {
        Assert.notNull(channelInitializer, "channelInitializer can't be null");
        Assert.notNull(properties, "ServerConfig can't be null");
        Assert.notNull(boss, "boss can't be null");
        Assert.notNull(work, "work can't be null");
        this.boss = boss;
        this.work = work;
        this.channelInitializer = channelInitializer;
        this.config = properties;
    }

    /**
     * 获取 EventLoopGroup 实例
     */
    public static EventLoopGroup newEventLoopGroup() {
        return Epoll.isAvailable() ? new EpollEventLoopGroup() : new NioEventLoopGroup();
    }

    /**
     * socket 服务
     */
    public void start() throws InterruptedException {
        var b = new ServerBootstrap();

        if (Epoll.isAvailable()) {
            b.channel(EpollServerSocketChannel.class);
        } else {
            b.channel(NioServerSocketChannel.class);
        }

        b
                .group(boss, work)
                .handler(new LoggingHandler(LogLevel.INFO))
                .option(ChannelOption.SO_REUSEADDR, true)
                .option(ChannelOption.SO_BACKLOG, config.getSoBacklog())
                .childHandler(channelInitializer);
        b.bind(config.getHost(), config.getPort()).sync();
    }
}
