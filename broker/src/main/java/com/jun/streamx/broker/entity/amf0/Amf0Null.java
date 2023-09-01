package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

/**
 * {@link Amf0Marker#NULL}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0Null extends Amf0CastFormat {

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.NULL;
    }

    @Override
    public void write(ByteBuf buf) {
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        return this;
    }
}
