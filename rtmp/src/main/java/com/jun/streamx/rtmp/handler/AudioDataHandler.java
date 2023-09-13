package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;

import java.util.Optional;

/**
 * {@link RtmpMessageType#AUDIO_DATA}
 *
 * @author Jun
 * @since 1.0.0
 */
@Handler(type = RtmpMessageType.AUDIO_DATA)
public class AudioDataHandler extends AbstractMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, RtmpMessage msg) {
        var streamKey = getSession(ctx).streamKey();
        Optional.ofNullable(subscribers.get(streamKey))
                .ifPresent(channels -> channels.forEach(channel -> {
                    // channel 协议检查
                    switch (protocol(channel)) {
                        case rtmp -> {
                            if (getSession(channel).isPause()) {
                                return;
                            }
                            channel.writeAndFlush(msg.retain());
                        }
                        case http, https -> channel.writeAndFlush(msg.toFlvAVTag());
                        case ws, wss -> channel.writeAndFlush(new BinaryWebSocketFrame(msg.toFlvAVTag()));
                    }
                }));
    }
}
