package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Amf0
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Amf0Number extends Amf0CastFormat {

    private Double value;

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.NUMBER;
    }

    @Override
    public void write(ByteBuf buf) {
        buf
                .writeByte(marker().val)
                .writeDouble(value);
    }

    @Override
    public Amf0Number read(ByteBuf src) {
        this.value = src.readDouble();
        return this;
    }
}
