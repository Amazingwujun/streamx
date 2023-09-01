package com.jun.streamx.broker.entity.amf0;

/**
 * 类型转换, 简化代码
 *
 * @author Jun
 * @since 1.0.0
 */
public abstract class Amf0CastFormat implements Amf0Format {

    @Override
    public <T> T cast(Class<T> clazz) {
        return (T) this;
    }
}
