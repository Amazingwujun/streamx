package com.jun.streamx.broker.handler.rtmp;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.RtmpMessage;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.*;

/**
 * 消息分发处理器
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
@Component
public class MessageDispatchHandler {

    private final Map<RtmpMessageType, MessageHandler> handlerMap = new HashMap<>();
    private final UnsupportedMessageHandler unsupportedMessageHandler = new UnsupportedMessageHandler();

    /**
     * 将处理器置入 {@link #handlerMap}
     *
     * @param rtmpMessageTypes 消息处理器
     */
    public MessageDispatchHandler(List<MessageHandler> rtmpMessageTypes) {
        Assert.notEmpty(rtmpMessageTypes, "messageHandlers can't be empty");

        // 置入处理器
        rtmpMessageTypes.forEach(messageHandler -> {
            var annotation = messageHandler.getClass().getAnnotation(Handler.class);
            Optional.ofNullable(annotation)
                    .map(Handler::type)
                    .ifPresent(rtmpMessageType -> handlerMap.put(rtmpMessageType, messageHandler));
        });
    }

    /**
     * 将消息委派给真正的 {@link MessageHandler}
     *
     * @param ctx         {@link ChannelHandlerContext}
     * @param rtmpMessage {@link RtmpMessage}
     */
    public void process(ChannelHandlerContext ctx, RtmpMessage rtmpMessage) {
        var messageType = rtmpMessage.messageType();

        var handler = handlerMap.get(messageType);
        if (handler != null) {
            handler.process(ctx, rtmpMessage);
        } else {
            unsupportedMessageHandler.process(ctx, rtmpMessage);
        }
    }


    private static final class UnsupportedMessageHandler implements MessageHandler {
        @Override
        public void process(ChannelHandlerContext ctx, RtmpMessage msg) {
            if (RtmpMessageType.SET_CHUNK_SIZE == msg.messageType()) {
                // set chunk size 在 RtmpChunkCodec 中处理了
                return;
            }
            log.warn("不支持的消息: {}", msg);
        }
    }
}
