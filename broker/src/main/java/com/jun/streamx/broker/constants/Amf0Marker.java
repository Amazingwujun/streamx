package com.jun.streamx.broker.constants;

import com.jun.streamx.broker.entity.Amf0Format;

/**
 * AMF0 marker
 *
 * @author Jun
 * @since 1.0.0
 */
public enum Amf0Marker {

    NUMBER(0),
    BOOLEAN(1),
    STRING(2),
    OBJECT(3),
    MOVIECLIP(4),
    NULL(5),
    UNDEFINED(6),
    REFERENCE(7),
    ECMA_ARRAY(8),
    OBJECT_END(9),
    STRICT_ARRAY(10),
    DATE(11),
    LONG_STRING(12),
    UNSUPPORTED(13),
    RECORDSET(14),
    XML_DOCUMENT(15),
    TYPED_OBJECT(16);

    private static final Amf0Marker[] VALUES;

    static {
        var values = values();
        VALUES = new Amf0Marker[values.length];
        for (var value : values) {
            VALUES[value.val] = value;
        }
    }

    public final int val;

    Amf0Marker(int i) {
        this.val = i;
    }

    public static Amf0Marker valueOf(int marker) {
        if (marker < 0 || marker > 16) {
            throw new IllegalArgumentException("非法的 marker: " + marker);
        }
        return VALUES[marker];
    }
}
