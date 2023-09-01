package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;

/**
 * {@link Amf0Marker#UNDEFINED}
 *
 * @author Jun
 * @since 1.0.0
 */
public class Amf0Undefined extends Amf0CastFormat {

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.UNDEFINED;
    }

    @Override
    public void write(ByteBuf buf) {
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        return this;
    }
}
