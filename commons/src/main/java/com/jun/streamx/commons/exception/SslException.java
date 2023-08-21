package com.jun.streamx.commons.exception;

/**
 * 证书相关异常
 *
 * @author Jun
 * @since 1.0.0
 */
public class SslException extends StreamxException {

    public SslException() {
    }

    public SslException(String message) {
        super(message);
    }

    public SslException(String message, Throwable cause) {
        super(message, cause);
    }

    public SslException(Throwable cause) {
        super(cause);
    }
}
