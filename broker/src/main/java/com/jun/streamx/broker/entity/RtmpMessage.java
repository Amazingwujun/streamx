package com.jun.streamx.broker.entity;

import com.jun.streamx.broker.constants.RtmpMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.util.internal.ReflectionUtil;
import io.netty.util.internal.ResourcesUtil;

/**
 * rtmp 消息.
 *
 * @author Jun
 * @since 1.0.0
 */
public class RtmpMessage {

    //@formatter:off

    private final RtmpMessageType messageType;
    private final int payloadLength;
    private final long timestamp;
    private final int streamId;

    private final ByteBuf payload;

    //@formatter:on


    public RtmpMessage(RtmpMessageType messageType,
                       int payloadLength,
                       long timestamp,
                       int streamId,
                       ByteBuf payload) {
        this.messageType = messageType;
        this.payloadLength = payloadLength;
        this.timestamp = timestamp;
        this.streamId = streamId;
        this.payload = payload;
    }

    public RtmpMessageType messageType() {
        return messageType;
    }

    public int payloadLength(){
        return payloadLength;
    }

    public long timestamp() {
        return timestamp;
    }

    public int streamId() {
        return streamId;
    }

    public ByteBuf payload() {
        return payload;
    }

    public void release() {
        this.payload.release();
    }

    @Override
    public String toString() {
        return "RtmpMessage{" +
                "messageType=" + messageType +
                ", payloadLength=" + payloadLength +
                ", timestamp=" + timestamp +
                ", streamId=" + streamId +
                ", payload=" + payload +
                '}';
    }
}
