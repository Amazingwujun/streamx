package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;

import java.util.Optional;

/**
 * {@link RtmpMessageType#VIDEO_DATA}
 *
 * @author Jun
 * @since 1.0.0
 */
@Handler(type = RtmpMessageType.VIDEO_DATA)
public class VideoDataHandler extends AbstractMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, RtmpMessage msg) {
        var session = getSession(ctx);
        var keyFrame = session.getKeyFrame();
        final var streamKey = session.streamKey();

        // key frame
        if (keyFrame == null && msg.isKeyFrame()) {
            session.setKeyFrame(msg.copy());

            // 加入推流端
            publishers.put(session.streamKey(), ctx.channel());
        } else {
            Optional.ofNullable(subscribers.get(streamKey))
                    .ifPresent(channels -> channels.forEach(channel -> channel.writeAndFlush(msg.retain())));
        }
    }
}
