package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * {@link RtmpMessageType#USER_CONTROL_MESSAGE}
 *
 * @author Jun
 * @since 1.0.0
 */
@Handler(type = RtmpMessageType.USER_CONTROL_MESSAGE)
public class UserControlMessageHandler implements MessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, RtmpMessage msg) {

    }
}
