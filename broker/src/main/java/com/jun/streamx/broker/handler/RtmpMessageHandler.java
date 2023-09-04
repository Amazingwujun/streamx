package com.jun.streamx.broker.handler;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import com.jun.streamx.broker.entity.amf0.*;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rtmp message handler
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class RtmpMessageHandler extends SimpleChannelInboundHandler<RtmpMessage> {

    public static final Map<String, ChannelGroup> APP_CHANNELS = new ConcurrentHashMap<>();
    private String app;
    private double connectTransactionId;
    private byte[] firstAudioPacket;
    private byte[] firstVideoPacket;

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("channel[{}] active", ctx.channel().id());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RtmpMessage msg) {
        log.info("收到 rtmp 报文: {}", msg);
        process(ctx, msg);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("channel[{}] inactive", ctx.channel().id());
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }


    private void process(ChannelHandlerContext ctx, RtmpMessage msg) {
        switch (msg.messageType()) {
            case AMF0_DATA -> handleAmf0Data(ctx, msg);
            case USER_CONTROL_MESSAGE -> handleUserControlMessage(ctx, msg);
            case AMF0_COMMAND -> handleAmf0Command(ctx, msg);
            case AUDIO_DATA -> handleAudioData(ctx, msg);
            case VIDEO_DATA -> handleVideoData(ctx, msg);
        }
    }

    private void handleAmf0Data(ChannelHandlerContext ctx, RtmpMessage msg) {
        // 解析出 cmd
        var list = msg.payloadToAmf0();
        if (list.isEmpty()) {
            log.error("非法的 AMF0_DATA 报文: {}", msg);
            ctx.close();
            return;
        }


    }

    private void handleUserControlMessage(ChannelHandlerContext ctx, RtmpMessage msg) {

    }

    private void handleAmf0Command(ChannelHandlerContext ctx, RtmpMessage msg) {
        // 解析出 cmd
        var list = msg.payloadToAmf0();
        if (list.isEmpty()) {
            log.error("非法的 AMF0_COMMAND 报文: {}", msg);
            ctx.close();
            return;
        }
        var amf0 = list.get(0).cast(Amf0String.class);
        var commandName = amf0.getValue();
        switch (commandName) {
            // NetConnection commands
            case "connect" -> {
                // The client sends the connect command to the server to request connection to a server application
                // instance.
                // command name + transaction id + command object + optional user arguments
                if (list.size() < 3) {
                    log.error("connect command structure error: {}", list);
                    ctx.close();
                }

                // transaction id
                var tid = list.get(1).cast(Amf0Number.class);

                // app name fetch
                var commandObject = list.get(2).cast(Amf0Object.class);
                this.app = commandObject.get("app").cast(Amf0String.class).getValue();

                // window acknowledgement size, set peer bandwidth, set chunk size
                var windowAcknowledgementSize = new RtmpMessage(
                        RtmpMessageType.WINDOW_ACKNOWLEDGEMENT_SIZE,
                        4, 0, 0,
                        Unpooled.buffer(4).writeInt(5_000_000)
                );
                ctx.write(windowAcknowledgementSize);
                var setPeerBandwidth = new RtmpMessage(
                        RtmpMessageType.SET_PEER_BANDWIDTH,
                        5, 0, 0,
                        Unpooled.buffer(5).writeInt(5_000_000).writeByte(2) // dynamic
                );
                ctx.write(setPeerBandwidth);
                var setChunkSize = new RtmpMessage(
                        RtmpMessageType.SET_CHUNK_SIZE,
                        4, 0, 0,
                        Unpooled.buffer(4).writeInt(1480)
                );
                ctx.write(setChunkSize);

                // _result, structure is command name + transaction id + properties + information
                var buf = Unpooled.buffer();
                var amf0FormatList = buildConnectResult(tid);
                amf0FormatList.forEach(t -> t.write(buf));
                var _result = new RtmpMessage(
                        RtmpMessageType.AMF0_COMMAND,
                        buf.readableBytes(), 0, 0,
                        buf
                );

                ctx.writeAndFlush(_result);
            }
            case "call" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "close" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "createStream" -> {
                // The client sends this command to the server to create a logical channel for message communication
                // The publishing of audio, video, and metadata is carried out over stream channel created using the
                // createStream command.
                //
                // NetConnection is the default communication channel, which has a stream ID 0. Protocol and a few
                // command messages, including createStream, use the default communication channel.

                // transaction id
                var tid = list.get(1).cast(Amf0Number.class);

                // _result
                var buf = Unpooled.buffer();
                var amf0FormatList = buildCreateStreamResult(tid);
                amf0FormatList.forEach(t -> t.write(buf));
                var _result = new RtmpMessage(
                        RtmpMessageType.AMF0_COMMAND,
                        buf.readableBytes(), 0, 0,
                        buf
                );

                ctx.writeAndFlush(_result);
            }
            // NetStream commands
            case "play" -> {
                // The client sends this command to the server to play a stream. A playlist can also be created using
                // this command multiple times.
                //
                // If you want to create a dynamic playlist that switches among different live or recorded streams,
                // call play more than once and pass false for reset each time. Conversely, if you want to play the
                // specified stream immediately, clearing any other streams that are queued for play, pass true for reset.

                var info = new Amf0Object();
                info.put("level", new Amf0String("status"));
                info.put("code", new Amf0String("NetStream.Play.Start"));
                info.put("description", new Amf0String("Start publishing"));

                var amf0FormatList = List.of(
                        Amf0String.ON_STATUS,
                        new Amf0Number(0d),
                        Amf0Null.INSTANCE,
                        info
                );

                // onStatus
                var buf = Unpooled.buffer();
                amf0FormatList.forEach(t -> t.write(buf));
                var onStatus = new RtmpMessage(RtmpMessageType.AMF0_COMMAND, 0, 0, buf);

                ctx.writeAndFlush(onStatus);
            }
            case "play2" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "deleteStream" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "closeStream" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "receiveAudio" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "receiveVideo" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "publish" -> {
                // The client sends the publish command to publish a named stream to the server. Using this name,
                // any client can play this stream and receive the published audio, video, and data messages.

                var info = new Amf0Object();
                info.put("level", new Amf0String("status"));
                info.put("code", new Amf0String("NetStream.Play.Start"));
                info.put("description", new Amf0String("Start publishing"));

                var amf0FormatList = List.of(
                        Amf0String.ON_STATUS,
                        new Amf0Number(0d),
                        Amf0Null.INSTANCE,
                        info
                );

                // onStatus
                var buf = Unpooled.buffer();
                amf0FormatList.forEach(t -> t.write(buf));
                var onStatus = new RtmpMessage(RtmpMessageType.AMF0_COMMAND, 0, 0, buf);

                ctx.writeAndFlush(onStatus);
            }
            case "seek" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "pause" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            // 抓包发现的 command
            case "FCPublish" -> {
                var info = new Amf0Object();
                info.put("level", new Amf0String("onFCPublish"));
                info.put("code", new Amf0String("NetStream.Play.Start"));
                info.put("description", new Amf0String("Start publishing"));
                var amf0FormatList = List.of(
                        new Amf0String("onFCPublish"),
                        Amf0Number.ZERO,
                        Amf0Null.INSTANCE,
                        info
                );

                var buf = Unpooled.buffer();
                amf0FormatList.forEach(t -> t.write(buf));
                var rm = new RtmpMessage(RtmpMessageType.AMF0_COMMAND, 0, 0, buf);

                ctx.writeAndFlush(rm);
            }
        }
    }


    private void handleAudioData(ChannelHandlerContext ctx, RtmpMessage msg) {
        // 记录下第一个 audio data
        if (firstAudioPacket == null) {
            this.firstAudioPacket = msg.readBytes();
        }
    }

    private void handleVideoData(ChannelHandlerContext ctx, RtmpMessage msg) {
        // 记录下第一个 video data
        if (firstVideoPacket == null) {
            this.firstVideoPacket = msg.readBytes();

            // todo key frame 检查
        }
    }

    private List<Amf0Format> buildConnectResult(Amf0Number tid) {
        var properties = new Amf0Object();
        properties.put("fmsVer", new Amf0String("FMS/3,0,1,123"));
        properties.put("capabilities", new Amf0Number(31d));
        var info = new Amf0Object();
        info.put("level", new Amf0String("status"));
        info.put("code", new Amf0String("NetConnection.Connect.Success"));
        info.put("description", new Amf0String("Connection succeeded."));
        info.put("objectEncoding", new Amf0Number(0d));
        return List.of(Amf0String._RESULT, tid, properties, info);
    }

    private List<Amf0Format> buildCreateStreamResult(Amf0Number tid) {
        return List.of(Amf0String._RESULT, tid, Amf0Null.INSTANCE, new Amf0Number(1d));
    }
}
