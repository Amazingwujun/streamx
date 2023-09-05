package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;

/**
 * rtmp {@link RtmpMessageType} 处理器
 *
 * @author Jun
 * @since 1.0.0
 */
public interface MessageHandler {

    void process(ChannelHandlerContext ctx, RtmpMessage msg);
}
