package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

/**
 * {@link Amf0Marker#BOOLEAN}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0Boolean extends Amf0CastFormat {

    public static final Amf0Boolean FALSE = new Amf0Boolean(false);
    public static final Amf0Boolean TRUE = new Amf0Boolean(true);

    private boolean value;

    public Amf0Boolean() {
    }

    public Amf0Boolean(boolean value) {
        this.value = value;
    }

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.BOOLEAN;
    }

    @Override
    public void write(ByteBuf buf) {
        buf
                .writeByte(marker().val)
                .writeBoolean(value);
    }

    @Override
    public Amf0Boolean read(ByteBuf src) {
        this.value = src.readBoolean();
        return this;
    }
}
