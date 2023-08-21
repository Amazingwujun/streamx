package com.jun.streamx.commons.exception;

/**
 * 项目顶级异常
 *
 * @author Jun
 * @since 1.0.0
 */
public class StreamxException extends RuntimeException {
    public StreamxException() {
        super();
    }

    public StreamxException(String message) {
        super(message);
    }

    public StreamxException(String format, Object... args) {
        super(String.format(format, args));
    }

    public StreamxException(String message, Throwable cause) {
        super(message, cause);
    }

    public StreamxException(Throwable cause) {
        super(cause);
    }
}
