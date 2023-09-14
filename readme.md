# StreamX



## 介绍
**StreamX** 项目旨在简化 rtsp/rtmp 转 http-flv 观看的应用。

**module**:

- commons：公共 module，提供一些工具类
- net：netty tcp server 封装
- broker：基于 javacv, netty 的应用，提供 rtsp/rtmp 拉流转 http-flv 功能
- rtmp：rtmp 服务器，支持 obs/ffmpeg 推流转 http-flv 观看



可使用 [bilibili/flv.js: HTML5 FLV Player (github.com)](https://github.com/Bilibili/flv.js) 播放 http-flv.



## 快速开始

### streamx-broker

打包

由于当前项目依赖 `ffmpeg`, 为减少打包 size，需要根据不同的 os 指定 Profile.

- windows 

```powershell
.\mvnw.cmd -DskipTests=true clean package -P !linux-x86_64,windows-x86_64
```

- linux

```shell
.\mvnw -DskipTests=true clean package -P linux-x86_64,!windows-x86_64
```



启动日志

```
      ___                       ___           ___           ___           ___           ___
     /  /\          ___        /  /\         /  /\         /  /\         /__/\         /__/|
    /  /:/_        /  /\      /  /::\       /  /:/_       /  /::\       |  |::\       |  |:|
   /  /:/ /\      /  /:/     /  /:/\:\     /  /:/ /\     /  /:/\:\      |  |:|:\      |  |:|
  /  /:/ /::\    /  /:/     /  /:/~/:/    /  /:/ /:/_   /  /:/~/::\   __|__|:|\:\   __|__|:|
 /__/:/ /:/\:\  /  /::\    /__/:/ /:/___ /__/:/ /:/ /\ /__/:/ /:/\:\ /__/::::| \:\ /__/::::\____
 \  \:\/:/~/:/ /__/:/\:\   \  \:\/:::::/ \  \:\/:/ /:/ \  \:\/:/__\/ \  \:\~~\__\/    ~\~~\::::/
  \  \::/ /:/  \__\/  \:\   \  \::/~~~~   \  \::/ /:/   \  \::/       \  \:\           |~~|:|~~
   \__\/ /:/        \  \:\   \  \:\        \  \:\/:/     \  \:\        \  \:\          |  |:|
     /__/:/          \__\/    \  \:\        \  \::/       \  \:\        \  \:\         |  |:|
     \__\/                     \__\/         \__\/         \__\/         \__\/         |__|/
    :: Project StreamX powered by https://github.com/Amazingwujun ::

2023-09-14 08:52:59.255  INFO 29660 --- [main] [55] c.jun.streamx.broker.BrokerApplication   : Starting BrokerApplication using Java 17.0.1 on DESKTOP-7TRMIPC with PID 29660 (C:\Users\admin\IdeaProjects\streamx\broker\target\classes started by admin in C:\Users\admin\IdeaProjects\streamx)
2023-09-14 08:52:59.257  INFO 29660 --- [main] [637] c.jun.streamx.broker.BrokerApplication   : The following 1 profile is active: "dev"
2023-09-14 08:52:59.732  INFO 29660 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x076c7912] REGISTERED
2023-09-14 08:52:59.733  INFO 29660 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x076c7912] BIND: /0.0.0.0:1989
2023-09-14 08:52:59.735  INFO 29660 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x076c7912, L:/[0:0:0:0:0:0:0:0]:1989] ACTIVE
2023-09-14 08:52:59.788  INFO 29660 --- [main] [61] c.jun.streamx.broker.BrokerApplication   : Started BrokerApplication in 0.863 seconds (JVM running for 1.254)
2023-09-14 08:52:59.794  INFO 29660 --- [main] [69] com.jun.streamx.commons.StreamxApp       : 配置打印: BizProperties(port=1989, webSocketPath=/live)
2023-09-14 08:52:59.796  INFO 29660 --- [main] [75] com.jun.streamx.commons.StreamxApp       : ---「streamx-broker」启动完成---
```



### streamx-rtmp

打包

- windows

```
.\mvnw.cmd -DskipTests=true clean package
```

- linux

```
.\mvnw -DskipTests=true clean package
```



启动日志

```
      ___                       ___           ___           ___           ___           ___
     /  /\          ___        /  /\         /  /\         /  /\         /__/\         /__/|
    /  /:/_        /  /\      /  /::\       /  /:/_       /  /::\       |  |::\       |  |:|
   /  /:/ /\      /  /:/     /  /:/\:\     /  /:/ /\     /  /:/\:\      |  |:|:\      |  |:|
  /  /:/ /::\    /  /:/     /  /:/~/:/    /  /:/ /:/_   /  /:/~/::\   __|__|:|\:\   __|__|:|
 /__/:/ /:/\:\  /  /::\    /__/:/ /:/___ /__/:/ /:/ /\ /__/:/ /:/\:\ /__/::::| \:\ /__/::::\____
 \  \:\/:/~/:/ /__/:/\:\   \  \:\/:::::/ \  \:\/:/ /:/ \  \:\/:/__\/ \  \:\~~\__\/    ~\~~\::::/
  \  \::/ /:/  \__\/  \:\   \  \::/~~~~   \  \::/ /:/   \  \::/       \  \:\           |~~|:|~~
   \__\/ /:/        \  \:\   \  \:\        \  \:\/:/     \  \:\        \  \:\          |  |:|
     /__/:/          \__\/    \  \:\        \  \::/       \  \:\        \  \:\         |  |:|
     \__\/                     \__\/         \__\/         \__\/         \__\/         |__|/
    :: Project StreamX powered by https://github.com/Amazingwujun ::

2023-09-14 08:52:28.170  INFO 27504 --- [main] [55] com.jun.streamx.rtmp.RtmpApplication     : Starting RtmpApplication using Java 17.0.1 on DESKTOP-7TRMIPC with PID 27504 (C:\Users\admin\IdeaProjects\streamx\rtmp\target\classes started by admin in C:\Users\admin\IdeaProjects\streamx)
2023-09-14 08:52:28.172  INFO 27504 --- [main] [637] com.jun.streamx.rtmp.RtmpApplication     : The following 1 profile is active: "dev"
2023-09-14 08:52:28.703  INFO 27504 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x07c0297a] REGISTERED
2023-09-14 08:52:28.707  INFO 27504 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x07c0297a] BIND: /0.0.0.0:1989
2023-09-14 08:52:28.709  INFO 27504 --- [nioEventLoopGroup-2-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x07c0297a, L:/[0:0:0:0:0:0:0:0]:1989] ACTIVE
2023-09-14 08:52:28.741  INFO 27504 --- [nioEventLoopGroup-4-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x68d453af] REGISTERED
2023-09-14 08:52:28.741  INFO 27504 --- [nioEventLoopGroup-4-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x68d453af] BIND: /0.0.0.0:1935
2023-09-14 08:52:28.742  INFO 27504 --- [nioEventLoopGroup-4-1] [148] io.netty.handler.logging.LoggingHandler  : [id: 0x68d453af, L:/[0:0:0:0:0:0:0:0]:1935] ACTIVE
2023-09-14 08:52:28.929  INFO 27504 --- [main] [61] com.jun.streamx.rtmp.RtmpApplication     : Started RtmpApplication in 1.111 seconds (JVM running for 1.551)
2023-09-14 08:52:28.932  INFO 27504 --- [main] [69] com.jun.streamx.commons.StreamxApp       : 配置打印: BizProperties(port=1935, httpFlv=BizProperties.HttpFlv(webSocketPath=/live, port=1989))
2023-09-14 08:52:28.934  INFO 27504 --- [main] [75] com.jun.streamx.commons.StreamxApp       : ---「streamx-rtmp」启动完成---
```



## 使用说明

### streamx-broker

> 仅测试支持 H.264 aac 编码格式

rtsp 源地址 `rtsp://admin:admin@camera-ip:port`

http-flv 播放地址 `ws://borker-ip:1989/live?src=rtsp://admin:admin@camera-ip:port` or `http://borker-ip:1989/live?src=rtsp://admin:admin@camera-ip:port`



### streamx-rtmp

> 仅测试支持 H.264 aac 编码格式

**obs** 推流地址 `rtmp://ip:1935/live` 推流码 `obs`

`ws://ip:1989/live?app=live&stream=obs` or `http://ip:1989/live?app=live&stream=obs`

使用 **vlc** 也可以播放



## 配置项

配置文件默认为 `application.yml`

| 配置                                    | 默认值  | 说明                 |
| --------------------------------------- | ------- | -------------------- |
| `streamx.broker.port`                   | 1989    | http, webSocket 端口 |
| `streamx.broker.web-socket-path`        | `/live` | webSocket path       |
| `streamx.rtmp.port`                     | 1935    | rtmp server port     |
| `streamx.rtmp.http-flv.port`            | 1989    | http, webSocket 端口 |
| `streamx.rtmp.http-flv.web-socket-path` | `live`  | webSocket path       |



## 已知问题

1、streamx-borker 只能播放视频，因为音频被我屏蔽了（见 `com.jun.streamx.broker.javacv.FrameGrabAndRecordManager`）。

2、stream-rtmp vlc 播放会自动停止，而 http-flv 确不会。这个问题的原因我暂时还没找到。

3、使用 flv.js 时 hasAudio 不能勾选，否则无法播放。
