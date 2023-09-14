package com.jun.streamx.rtmp.entity.amf0;

import com.jun.streamx.rtmp.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;


/**
 * {@link Amf0Marker#ECMA_ARRAY}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0EcmaArray extends LinkedHashMap<String, Amf0Format> implements Amf0Format {

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.ECMA_ARRAY;
    }

    @Override
    public void write(ByteBuf buf) {
        // marker
        buf.writeByte(marker().val);
        // array len
        buf.writeInt(this.size());
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
        buf.writeBytes(Amf0Object.END);
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        // 长度读取
        var size = src.readUnsignedInt();
        // properties 读取
        var len = src.readUnsignedShort();
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
    public <T extends Amf0Format> T cast(Class<T> clazz) {
        return (T) this;
    }
}
