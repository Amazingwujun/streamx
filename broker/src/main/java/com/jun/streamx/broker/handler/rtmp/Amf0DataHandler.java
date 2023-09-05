package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import com.jun.streamx.broker.entity.amf0.Amf0String;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link RtmpMessageType#AMF0_DATA}
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Handler(type = RtmpMessageType.AMF0_DATA)
public class Amf0DataHandler extends AbstractMessageHandler {

    @Override
    public void process(ChannelHandlerContext ctx, RtmpMessage msg) {
        var session = getSession(ctx);

        // 解析
        var list = msg.payloadToAmf0();
        if (list.isEmpty()) {
            log.error("非法的 AMF0_DATA 报文: {}", msg);
            ctx.close();
            return;
        }

        // 我们需要 metadata
        for (int i = 0; i < list.size(); i++) {
            var amf0Format = list.get(i);
            if (amf0Format instanceof Amf0String s) {
                if (Amf0String.ON_META_DATA.getValue().equals(s.getValue())) {
                    // 表明下一个 Amf0Object(nginx-rtmp-module) 或 Amf0EcmaArray(obs)
                    var metadata = list.get(i + 1);
                    session.setMetadata(metadata);
                    break;
                }
            }
        }
    }
}
