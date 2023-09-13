package com.jun.streamx.commons.constants;

/**
 * 协议枚举
 *
 * @author Jun
 * @since 1.0.0
 */
public enum Protocol {
    ws, wss, http, https, rtmp;

    public final static String key = "PROTOCOL_KEY";
}
