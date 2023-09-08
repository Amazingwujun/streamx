package com.jun.streamx.rtmp.entity.amf0;

import com.jun.streamx.commons.exception.UnsupportedParamsException;
import com.jun.streamx.rtmp.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * Rtmp AMF0 format
 *
 * @author Jun
 * @since 1.0.0
 */
public interface Amf0Format extends Cast {

    /**
     * 返回当前 AMF0 对象的 marker.
     *
     * @return amf0 marker
     */
    Amf0Marker marker();

    /**
     * 将当前 AMF0 对象写入 dst
     *
     * @param dst 目标缓存
     */
    void write(ByteBuf dst);

    /**
     * 从 src 中解读出数据内容, <strong>注意: marker 已被读取</strong>
     *
     * @param src 数据源
     */
    Amf0Format read(ByteBuf src);

    static List<Amf0Format> parse(ByteBuf payload) {
        var lst = new ArrayList<Amf0Format>();
        while (payload.isReadable()) {
            lst.add(parse0(payload));
        }
        return lst;
    }

    static Amf0Format parse0(ByteBuf in) {
        var marker = Amf0Marker.valueOf(in.readByte());
        Amf0Format result;
        switch (marker) {
            case NUMBER -> result = new Amf0Number().read(in);
            case BOOLEAN -> result = new Amf0Boolean().read(in);
            case STRING -> result = new Amf0String().read(in);
            case OBJECT -> result = new Amf0Object().read(in);
            case NULL -> result = new Amf0Null().read(in);
            case UNDEFINED -> result = new Amf0Undefined().read(in);
            case REFERENCE -> result = new Amf0Reference().read(in);
            case ECMA_ARRAY -> result = new Amf0EcmaArray().read(in);
            default -> throw new UnsupportedParamsException("不支持的 AMF0 类型: %s", marker);
        }

        return result;
    }
}
