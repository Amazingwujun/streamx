package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
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
