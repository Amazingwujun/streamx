package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
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
                .ifPresent(channels -> channels.forEach(channel -> channel.writeAndFlush(msg.retain())));
    }
}
