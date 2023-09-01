package com.jun.streamx.broker.exception;

import com.jun.streamx.commons.exception.StreamxException;

/**
 * 不支持的参数异常
 *
 * @author Jun
 * @since 1.0.0
 */
public class UnsupportedParamsException extends StreamxException {

    public UnsupportedParamsException(String format, Object... args) {
        super(format, args);
    }
}
