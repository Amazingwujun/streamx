package com.jun.streamx.broker.entity;

import com.jun.streamx.broker.constants.RtmpMessageType;
import com.jun.streamx.broker.entity.amf0.Amf0Format;
import io.netty.buffer.ByteBuf;
import io.netty.util.ReferenceCounted;

import java.util.List;

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
                       long timestamp,
                       int streamId,
                       ByteBuf payload) {
        this(messageType, payload.readableBytes(), timestamp, streamId, payload);
    }

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

    public int payloadLength() {
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

    /**
     * @see ByteBuf#readBytes(byte[])
     */
    public void readBytes(byte[] dst) {
        payload.readBytes(dst);
    }

    /**
     * 读取全部数据
     *
     * @return payload 中的全部字节数组
     */
    public byte[] readBytes() {
        return readBytes(payload.readableBytes());
    }

    /**
     * 读取指定长度 buf
     *
     * @param len 需要读取字节长度
     */
    public byte[] readBytes(int len) {
        var buf = new byte[len];
        payload.readBytes(buf);
        return buf;
    }

    public boolean release() {
        return this.payload.release();
    }

    public boolean release(int decrement) {
        return this.payload.release(decrement);
    }

    public RtmpMessage replaced() {
        return new RtmpMessage(messageType, timestamp, streamId, payload.copy());
    }

    public RtmpMessage retain() {
        this.payload.retain();
        return this;
    }

    /**
     * 将当前的 payload 解析为 AMF0 格式的数据
     *
     * @return AMF0 格式数据
     */
    public List<Amf0Format> payloadToAmf0() {
        return Amf0Format.parse(payload);
    }

    @Override
    public String toString() {
        switch (messageType) {
            case AMF0_DATA, AMF0_COMMAND -> {
                this.payload.markReaderIndex();
                var amf0 = payloadToAmf0();
                this.payload.resetReaderIndex();
                return "RtmpMessage{" +
                        "messageType=" + messageType +
                        ", payloadLength=" + payloadLength +
                        ", timestamp=" + timestamp +
                        ", streamId=" + streamId +
                        ", payload=" + amf0 +
                        '}';
            }
            default -> {
                return "RtmpMessage{" +
                        "messageType=" + messageType +
                        ", payloadLength=" + payloadLength +
                        ", timestamp=" + timestamp +
                        ", streamId=" + streamId +
                        ", payload=" + payload +
                        '}';
            }
        }
    }
}
