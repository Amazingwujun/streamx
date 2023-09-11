package com.jun.streamx.rtmp.constants;

/**
 * 用户控制报文事件类型枚举
 *
 * @author Jun
 * @since 1.0.0
 */
public enum UserControlMessageEvent {

    /**
     * The server sends this event to notify the client that a stream has become functional and can be used for
     * communication. By default, this event is sent on ID 0 after the application connect command is successfully
     * received from the client. The event data is 4-byte and represents the stream ID of the stream that became
     * functional.
     */
    STREAM_BEGIN(0),
    /**
     * The server sends this event to notify the client that the playback of data is over as requested on this stream.
     * No more data is sent without issuing additional commands. The client discards the messages received for the
     * stream. The 4 bytes of event data represent the ID of the stream on which playback has ended.
     */
    STREAM_EOF(1),
    /**
     * 	The server sends this event to notify the client that there is no more data on the stream. If the server does
     * 	not detect any message for a time period, it can notify the subscribed clients that the stream is dry. The 4
     * 	bytes of event data represent the stream ID of the dry stream.
     */
    STREAM_DRY(2),
    /**
     * The client sends this event to inform the server of the buffer size (in milliseconds) that is used to buffer any
     * data coming over a stream. This event is sent before the server starts processing the stream. The first 4 bytes
     * of the event data represent the stream ID and the next 4 bytes represent the buffer length, in milliseconds.
     */
    SET_BUFFER_LENGTH(3),
    /**
     * The server sends this event to notify the client that the stream is a recorded stream. The 4 bytes event data
     * represent the stream ID of the recorded stream.
     */
    STREAMLS_RECORDED(4),
    /**
     * The server sends this event to test whether the client is reachable. Event data is a 4-byte timestamp,
     * representing the local server time when the server dispatched the command. The client responds with PingResponse
     * on receiving MsgPingRequest.
     */
    PING_REQUEST(6),
    /**
     * The client sends this event to the server in response to the ping request. The event data is a 4-byte timestamp,
     * which was received with the PingRequest request.
     */
    PING_RESPONSE(7);

    public final int val;

    UserControlMessageEvent(int val) {
        this.val = val;
    }

    public static UserControlMessageEvent valueOf(int eventType) {
        UserControlMessageEvent event;
        switch (eventType) {
            case 0 -> event = STREAM_BEGIN;
            case 1 -> event = STREAM_EOF;
            case 2 -> event = STREAM_DRY;
            case 3 -> event = SET_BUFFER_LENGTH;
            case 4 -> event = STREAMLS_RECORDED;
            case 6 -> event = PING_REQUEST;
            case 7 -> event = PING_RESPONSE;
            default -> throw new IllegalArgumentException("非法的 user event type: "+ eventType);
        }
        return event;
    }
}
