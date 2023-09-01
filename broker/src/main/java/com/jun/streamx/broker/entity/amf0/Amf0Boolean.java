package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;
import lombok.Data;

/**
 * {@link Amf0Marker#BOOLEAN}
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
public class Amf0Boolean extends Amf0CastFormat {

    private boolean value;

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
