package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link RtmpMessageType#ACKNOWLEDGEMENT}
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Handler(type = RtmpMessageType.ACKNOWLEDGEMENT)
public class AcknowledgementHandler implements MessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, RtmpMessage msg) {
        log.info("收到 Acknowledgement 报文: {}", msg);
    }
}
