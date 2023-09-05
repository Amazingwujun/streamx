package com.jun.streamx.broker.entity.amf0;

import com.jun.streamx.broker.constants.Amf0Marker;
import io.netty.buffer.ByteBuf;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.nio.charset.StandardCharsets;

/**
 * {@link Amf0Marker#STRING}
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
public class Amf0String extends Amf0CastFormat {

    public static final Amf0String _RESULT = new Amf0String("_result");
    public static final Amf0String _ERROR = new Amf0String("_error");
    public static final Amf0String ON_STATUS = new Amf0String("onStatus");
    public static final Amf0String ON_META_DATA = new Amf0String("onMetaData");
    public static final Amf0String ON_FC_PUBLISH = new Amf0String("onFCPublish");
    public static final Amf0String STATUS = new Amf0String("status");
    public static final Amf0String RTMP_SAMPLE_ACCESS = new Amf0String("|RtmpSampleAccess");

    private String value;

    @Override
    public Amf0Marker marker() {
        return Amf0Marker.STRING;
    }

    @Override
    public void write(ByteBuf buf) {
        buf
                .writeByte(marker().val)
                .writeShort(value.length())
                .writeBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Amf0Format read(ByteBuf src) {
        int len = src.readUnsignedShort();
        var buf = new byte[len];
        src.readBytes(buf);
        this.value = new String(buf);
        return this;
    }
}
