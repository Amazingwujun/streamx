package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;

/**
 * {@link Amf0Marker#OBJECT}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0Object extends LinkedHashMap<String, Amf0Format> implements Amf0Format {

    public static final byte[] END = {0, 0, (byte) Amf0Marker.OBJECT_END.val};

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.OBJECT;
    }

    @Override
    public void write(ByteBuf buf) {
        // marker
        buf.writeByte(marker().val);
        // properties
        this.forEach((k, v) -> {
            // name
            buf
                    .writeShort(k.length())
                    .writeBytes(k.getBytes(StandardCharsets.UTF_8));
            // value
            v.write(buf);
        });
        // object end
        buf.writeBytes(END);
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        int len = src.readUnsignedShort();
        while (len != 0) {
            var buf = new byte[len];
            src.readBytes(buf);
            var name = new String(buf);
            this.put(name, Amf0Format.parse0(src));
            len = src.readUnsignedShort();
        }

        // 读 OBJECT_END
        var m = Amf0Marker.valueOf(src.readByte());
        if (Amf0Marker.OBJECT_END != m) {
            throw new IllegalArgumentException("Unexpected AMF0 object end: {}" + m);
        }
        return this;
    }

    @Override
    public <T> T cast(Class<T> clazz) {
        return (T) this;
    }
}
