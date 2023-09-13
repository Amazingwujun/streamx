package com.jun.streamx.rtmp.entity;

import com.jun.streamx.commons.utils.ByteUtils;
import com.jun.streamx.rtmp.entity.amf0.Amf0Format;
import com.jun.streamx.rtmp.entity.amf0.Amf0String;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Rtmp 会话对象
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class RtmpSession extends CompletableFuture<RtmpSession.State> {

    private static final byte[] FLV_HEADER = new byte[]{0x46, 0x4c, 0x56, 0x01, 0x05, 0x00, 0x00, 0x00, 0x09};

    public enum Type {
        publisher,
        subscriber
    }

    public enum State {
        complete,
        inactive
    }

    //@formatter:off
    public static final String KEY = "rtmp-session";

    /** app */
    private String app;

    /** 流名称 */
    private String streamName;

    /** metadata 字节数组 */
    private Amf0Format metadata;

    /** AVC sequence header 数据包 */
    private RtmpMessage keyFrame;

    /** 视频时间戳 */
    private long latestVideoTimestamp;

    private Type type;

    /**
     * pause flag
     * <ol>
     *     <li>true: 暂停推流</li>
     *     <li>false: 启动推流</li>
     * </ol>
     */
    private volatile boolean pause;

    private ByteBuf flvHeaders;

    //@formatter:on


    public String streamKey() {
        return String.format("%s/%s", app, streamName);
    }

    public void release() {
        this.keyFrame.release();
        this.flvHeaders.release();
    }

    public void setKeyFrame(RtmpMessage keyFrame) {
        this.keyFrame = keyFrame;
        if (isArgComplete()) {
            this.complete(State.complete);
        }

        // flv headers 处理
        if (flvHeaders != null) {
            return;
        }

        // 组装 flvHeaders
        flvHeaders = Unpooled.buffer()
                .writeBytes(FLV_HEADER)
                .writeInt(0); // pre tag size

        // script tag data
        var scriptData = Unpooled.buffer();
        List.of(Amf0String.ON_META_DATA, metadata).forEach(t -> t.write(scriptData));
        var scriptDataLen = scriptData.readableBytes();

        // script tag header
        flvHeaders
                .writeByte(0x12) // tag type
                .writeMedium(scriptDataLen) // data size
                .writeMedium(0) // timestamp
                .writeByte(0) // timestamp extended
                .writeMedium(0); // stream id
        flvHeaders.writeBytes(scriptData);
        flvHeaders.writeInt(scriptDataLen + 11); // pre tag size
        scriptData.release();

        // key frame, first video frame
        var keyFrameLen = keyFrame.payload().readableBytes();
        flvHeaders
                .writeByte(0x09)
                .writeMedium(keyFrameLen)
                .writeMedium(0)
                .writeByte(0)
                .writeMedium(0);
        flvHeaders.writeBytes(keyFrame.payload().duplicate());
        flvHeaders.writeInt(keyFrameLen + 11);
    }

    public void setMetadata(Amf0Format metadata) {
        this.metadata = metadata;
        if (isArgComplete()) {
            this.complete(State.complete);
        }
    }

    /**
     * 判断 publish stream 必要参数是否完整.
     */
    @SuppressWarnings("RedundantIfStatement")
    private boolean isArgComplete() {
        if (ObjectUtils.isEmpty(app)) {
            return false;
        }
        if (ObjectUtils.isEmpty(streamName)) {
            return false;
        }
        if (metadata == null) {
            return false;
        }
        if (keyFrame == null) {
            return false;
        }

        return true;
    }
}
