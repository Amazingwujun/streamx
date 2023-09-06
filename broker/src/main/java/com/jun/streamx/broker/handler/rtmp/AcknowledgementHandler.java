package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
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
        log.debug("收到 Acknowledgement 报文: {}", msg);
    }
}
