package com.jun.streamx.rtmp.constants;

/**
 * rtmp 消息类型
 *
 * @author Jun
 * @since 1.0.0
 */
public enum RtmpMessageType {

    /**
     * RTMP Chunk Stream uses message type IDs 1, 2, 3, 5, and 6 for
     * protocol control messages. These messages contain information needed
     * by the RTMP Chunk Stream protocol.(rtmp_specification_1.0[5.4. Protocol Control Messages])
     */

    SET_CHUNK_SIZE(0x01),
    ABORT_MESSAGE(0x02),
    ACKNOWLEDGEMENT(0x03),
    WINDOW_ACKNOWLEDGEMENT_SIZE(0x05),
    SET_PEER_BANDWIDTH(0x06),

    USER_CONTROL_MESSAGE(0x04),
    AMF3_COMMAND(0x11),
    AMF0_COMMAND(0x14),
    AMF0_DATA(0x12),
    AMF3_DATA(0x0f),
    AMF0_SHARED_OBJECT(0x13),
    AMF3_SHARED_OBJECT(0x10),
    AUDIO_DATA(0x08),
    VIDEO_DATA(0x09),
    AGGREGATE_MESSAGE(0x16);

    public final int val;

    RtmpMessageType(int i) {
        this.val = i;
    }

    public static RtmpMessageType valueOf(int type) {
        RtmpMessageType typeId;
        switch (type) {
            case 0x01 -> typeId = SET_CHUNK_SIZE;
            case 0x02 -> typeId = ABORT_MESSAGE;
            case 0x03 -> typeId = ACKNOWLEDGEMENT;
            case 0x05 -> typeId = WINDOW_ACKNOWLEDGEMENT_SIZE;
            case 0x06 -> typeId = SET_PEER_BANDWIDTH;

            case 0x04 -> typeId = USER_CONTROL_MESSAGE;
            case 0x11 -> typeId = AMF3_COMMAND;
            case 0x14 -> typeId = AMF0_COMMAND;
            case 0x12 -> typeId = AMF0_DATA;
            case 0x0f -> typeId = AMF3_DATA;
            case 0x13 -> typeId = AMF0_SHARED_OBJECT;
            case 0x10 -> typeId = AMF3_SHARED_OBJECT;
            case 0x08 -> typeId = AUDIO_DATA;
            case 0x09 -> typeId = VIDEO_DATA;
            case 0x16 -> typeId = AGGREGATE_MESSAGE;
            default -> throw new IllegalArgumentException("非法的 typeId: " + type);
        }
        return typeId;
    }
}
