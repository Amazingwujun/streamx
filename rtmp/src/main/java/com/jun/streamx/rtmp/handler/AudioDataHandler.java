package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;

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
                    if (getSession(channel).isPause()) {
                        return;
                    }
                    channel.writeAndFlush(msg.retain());
                }));
    }
}
