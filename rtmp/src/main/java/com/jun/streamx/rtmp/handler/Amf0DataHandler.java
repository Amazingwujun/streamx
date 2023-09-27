package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.RtmpMessage;
import com.jun.streamx.rtmp.entity.amf0.Amf0Format;
import com.jun.streamx.rtmp.entity.amf0.Amf0String;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;

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
                    var metadata = (LinkedHashMap<String, Amf0Format>)list.get(i + 1);
                    session.setMetadata(metadata);
                    break;
                }
            }
        }
    }
}
