package com.jun.streamx.broker.handler;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
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
        log.debug("收到 rtmp 报文: {}", msg);
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

    }

    private void handleUserControlMessage(ChannelHandlerContext ctx, RtmpMessage msg) {

    }

    private void handleAmf0Command(ChannelHandlerContext ctx, RtmpMessage msg) {
        // 解析出 cmd
        var list = msg.payloadToAmf0();
        if (list.isEmpty()) {
            log.error("非法的 AMF0_COMMAND 报文: {}", msg);
            ctx.close();
        }
        var tuple = list.get(0);
        var commandName = (String) tuple.t1();
        switch (commandName) {
            // NetConnection commands
            case "connect" -> {
                // command name + transaction id + command object + optional user arguments
                if (list.size() < 3) {
                    log.error("connect command structure error: {}", list);
                    ctx.close();
                }

                // transaction id
                var tid = (Double)list.get(1).t1();

                // app name fetch
                @SuppressWarnings("unchecked")
                var commandObject = (LinkedHashMap<String, Object>) list.get(2).t1();
                this.app = (String) commandObject.get("app");

                // transaction id


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
                var setChunkSize = new RtmpMessage(
                        RtmpMessageType.SET_CHUNK_SIZE,
                        4, 0, 0,
                        Unpooled.buffer(4).writeInt(1480)
                );
                ctx.write(setChunkSize);

                // _result, structure is command name + transaction id + properties + information
                var _result = new RtmpMessage(
                        RtmpMessageType.AMF0_COMMAND,
                        4, 0, 0,
                        Unpooled.buffer()
                );
            }
            case "call" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "close" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "createStream" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            // NetStream commands
            case "play" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "play2" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "deleteStream" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "closeStream" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "receiveAudio" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "receiveVideo" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "publish" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "seek" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
            case "pause" -> throw new UnsupportedOperationException("不支持的 cmd: " + commandName);
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
}
