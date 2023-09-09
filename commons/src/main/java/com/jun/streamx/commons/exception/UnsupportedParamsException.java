package com.jun.streamx.commons.exception;

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
