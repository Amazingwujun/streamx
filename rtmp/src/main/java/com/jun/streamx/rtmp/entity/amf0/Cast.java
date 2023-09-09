package com.jun.streamx.rtmp.entity.amf0;

/**
 * 类型转换及匹配接口
 *
 * @author Jun
 * @since 1.0.0
 */
public interface Cast {

    <T> T cast(Class<T> clazz);
}
