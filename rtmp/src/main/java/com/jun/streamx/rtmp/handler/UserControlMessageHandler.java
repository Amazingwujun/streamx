package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
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
