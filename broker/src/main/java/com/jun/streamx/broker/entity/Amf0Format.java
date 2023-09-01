package com.jun.streamx.broker.entity;

import com.jun.streamx.broker.constants.Amf0Marker;
import com.jun.streamx.broker.exception.UnsupportedParamsException;
import com.jun.streamx.commons.entity.Tuple2;
import com.jun.streamx.commons.entity.Tuples;
import io.netty.buffer.ByteBuf;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Rtmp AMF0 format
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class Amf0Format {

    public static List<Tuple2<Amf0Marker, Object>> parse(ByteBuf payload) {
        var lst = new ArrayList<Tuple2<Amf0Marker, Object>>();
        while (payload.isReadable()) {
            lst.add(parse0(payload));
        }
        return lst;
    }

    private static Tuple2<Amf0Marker, Object> parse0(ByteBuf in) {
        var marker = Amf0Marker.valueOf(in.readByte());
        switch (marker) {
            case NUMBER -> {
                return Tuples.of(marker, in.readDouble());
            }
            case BOOLEAN -> {
                return Tuples.of(marker, in.readBoolean());
            }
            case STRING -> {
                int len = in.readUnsignedShort();
                var buf = new byte[len];
                in.readBytes(buf);
                return Tuples.of(marker, new String(buf));
            }
            case OBJECT -> {
                var lh = new LinkedHashMap<String, Object>();
                int len = in.readUnsignedShort();
                while (len != 0) {
                    var buf = new byte[len];
                    in.readBytes(buf);
                    var name = new String(buf);
                    lh.put(name, parse0(in));
                    len = in.readUnsignedShort();
                }

                // 读 OBJECT_END
                var m = Amf0Marker.valueOf(in.readByte());
                if (Amf0Marker.OBJECT_END != m) {
                    log.error("object end 未能读取");
                    throw new IllegalArgumentException("Unexpected AMF0 object end: {}" + m);
                }

                return Tuples.of(marker, lh);
            }
            case NULL, UNDEFINED -> {
                return Tuples.of(marker, null);
            }
            case REFERENCE -> {
                return Tuples.of(marker, in.readUnsignedShort());
            }
            case ECMA_ARRAY -> {
                // 数组长度
                var lh = new LinkedHashMap<String, Object>();
                in.skipBytes(4); // 跳过数组长度
                var len = in.readUnsignedShort();
                while (len != 0) {
                    var buf = new byte[len];
                    in.readBytes(buf);
                    var name = new String(buf);
                    lh.put(name, parse0(in));
                    len = in.readUnsignedShort();
                }

                // 读 OBJECT_END
                var m = Amf0Marker.valueOf(in.readByte());
                if (Amf0Marker.OBJECT_END != m) {
                    log.error("object end 未能读取");
                    throw new IllegalArgumentException("Unexpected AMF0 object end: {}" + m);
                }

                return Tuples.of(marker, lh);
            }
            default -> throw new UnsupportedParamsException("不支持的 AMF0 类型: %s", marker);
        }
    }
}
