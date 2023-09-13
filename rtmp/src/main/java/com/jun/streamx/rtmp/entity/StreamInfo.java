package com.jun.streamx.rtmp.entity;

/**
 * 流信息
 *
 * @param app app名称
 * @param stream 推流码
 * @author Jun
 * @since 1.0.0
 */
public record StreamInfo(String app, String stream) {

    public String key() {
        return String.format("%s/%s", app, stream);
    }
}
