package com.jun.streamx.broker.entity;

import com.jun.streamx.broker.entity.amf0.Amf0Format;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Rtmp 会话对象
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@Accessors(chain = true)
public class RtmpSession {

    public enum Type {
        publisher,
        subscriber
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

    //@formatter:on


    public String streamKey() {
        return String.format("%s/%s", app, streamName);
    }
}
