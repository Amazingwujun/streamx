package com.jun.streamx.rtmp.entity;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.rtmp.entity.amf0.Amf0Format;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import java.util.List;

/**
 * rtmp 消息.
 *
 * @author Jun
 * @since 1.0.0
 */
public class RtmpMessage implements ByteBufHolder {
    //@formatter:off

    private final RtmpMessageType messageType;
    /** 由于需要对 payload 进行写入操作，payloadLength 数据不一定正确 */
    private final int payloadLength;
    private final long timestamp;
    private final int streamId;

    private final ByteBuf payload;

    //@formatter:on

    public RtmpMessage(RtmpMessageType messageType) {
        this(messageType, 0, 0, Unpooled.buffer());
    }

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
        return content().release();
    }

    public boolean release(int decrement) {
        return content().release(decrement);
    }

    public RtmpMessage replaced() {
        return new RtmpMessage(messageType, timestamp, streamId, payload.copy());
    }

    @Override
    public ByteBuf content() {
        return ByteBufUtil.ensureAccessible(payload);
    }

    @Override
    public RtmpMessage copy() {
        return replace(content().copy());
    }

    @Override
    public RtmpMessage duplicate() {
        return replace(content().duplicate());
    }

    @Override
    public RtmpMessage retainedDuplicate() {
        return replace(content().retainedDuplicate());
    }

    @Override
    public RtmpMessage replace(ByteBuf content) {
        return new RtmpMessage(messageType, timestamp, streamId, content);
    }

    @Override
    public int refCnt() {
        return content().refCnt();
    }

    public RtmpMessage retain() {
        content().retain();
        return this;
    }

    @Override
    public RtmpMessage retain(int increment) {
        content().retain(increment);
        return null;
    }

    @Override
    public RtmpMessage touch() {
        content().touch();
        return this;
    }

    @Override
    public RtmpMessage touch(Object hint) {
        content().touch(hint);
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

    /**
     * 检查数据包是AVC sequence header，即AVC格式的视频序列头信息。
     *
     * @return true if video data is key frame
     */
    public boolean isAVCNALU() {
        if (RtmpMessageType.VIDEO_DATA != messageType) {
            throw new UnsupportedOperationException("不支持 " + messageType + " 类型进行 key frame 判断");
        }

        content().markReaderIndex();
        byte b1 = content().readByte();
        byte b2 = content().readByte();
        content().resetReaderIndex();
        return b1 == 0x17 && b2 == 1;
    }

    /**
     * 检查数据包是AVC NALU（Network Abstraction Layer Unit），即包含视频帧数据的NALU单元。
     */
    public boolean isAVCSequenceHeader() {
        if (RtmpMessageType.VIDEO_DATA != messageType) {
            throw new UnsupportedOperationException("不支持 " + messageType + " 类型进行 key frame 判断");
        }

        content().markReaderIndex();
        byte b1 = content().readByte();
        byte b2 = content().readByte();
        content().resetReaderIndex();
        return b1 == 0x17 && b2 == 0;
    }

    /**
     * key frame 检查
     *
     * @return true if video frame is key frame
     */
    public boolean isKeyFrame() {
        if (RtmpMessageType.VIDEO_DATA != messageType) {
            throw new UnsupportedOperationException("不支持 " + messageType + " 类型进行 key frame 判断");
        }

        content().markReaderIndex();
        byte b1 = content().readByte();
        content().resetReaderIndex();
        return b1 == 0x17;
    }

    public ByteBuf toFlvAVTag() {
        var buf = Unpooled.buffer();
        switch (messageType) {
            case VIDEO_DATA -> buf.writeByte(9);
            case AUDIO_DATA -> buf.writeByte(8);
            default -> throw new UnsupportedOperationException("不支持的类型: " + messageType);
        }
        buf.writeMedium(payload.readableBytes());
        if (timestamp > 0xffffff) {
            buf.writeMedium((int) (timestamp & 0xffffff));
            buf.writeByte((int) ((timestamp & 0xffffffffL) >> 24));
        } else {
            buf.writeMedium((int) timestamp);
            buf.writeByte(0);
        }
        buf.writeMedium(0);
        buf.writeBytes(payload.duplicate());
        buf.writeInt(11 + payload.readableBytes());

        return buf;
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
