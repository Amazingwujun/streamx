package com.jun.streamx.rtmp.entity;

import com.jun.streamx.rtmp.entity.amf0.Amf0Format;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.util.ObjectUtils;

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

    //@formatter:on


    public String streamKey() {
        return String.format("%s/%s", app, streamName);
    }

    public void release() {
        this.keyFrame.release();
    }

    public void setKeyFrame(RtmpMessage keyFrame) {
        this.keyFrame = keyFrame;
        if (isArgComplete()) {
            this.complete(State.complete);
        }
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
