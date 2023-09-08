package com.jun.streamx.rtmp.entity.amf0;

import com.jun.streamx.rtmp.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

/**
 * {@link Amf0Marker#UNDEFINED}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0Reference extends Amf0CastFormat {

    private int value;

    public Amf0Reference() {
    }

    public Amf0Reference(int value) {
        this.value = value;
    }

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.REFERENCE;
    }

    @Override
    public void write(ByteBuf dst) {
        dst
                .writeInt(marker().val)
                .writeShort(value);
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        this.value = src.readUnsignedShort();
        return this;
    }
}
