package com.jun.streamx.rtmp.handler;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Rtmp 消息处理器
 *
 * @author Jun
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface Handler {

    /**
     * handle 支持的消息类型
     */
    RtmpMessageType[] type() default {};
}
