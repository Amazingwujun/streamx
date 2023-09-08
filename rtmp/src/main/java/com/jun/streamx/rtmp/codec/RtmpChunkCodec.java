package com.jun.streamx.rtmp.codec;

import com.jun.streamx.rtmp.constants.RtmpMessageType;
import com.jun.streamx.commons.exception.StreamxException;
import com.jun.streamx.rtmp.entity.RtmpMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * rtmp chunk message decoder
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class RtmpChunkCodec extends ByteToMessageCodec<RtmpMessage> {

    private enum DecodeState {
        READ_HEADER,
        READ_BODY
    }

    private enum FmtEnum {
        //@formatter:off
        /** base header(1 byte) + timestamp(3 byte) + body size(3 byte) + type id(1 byte) + stream id(4 byte) */
        FMT_00(0b00),
        /** base header(1 byte) + timestamp delta(3 byte) + body size(3 byte) + type id(1 byte)  */
        FMT_01(0b01),
        /** base header(1 byte) + timestamp delta(3 byte) */
        FMT_10(0b10),
        /** base header(1 byte) */
        FMT_11(0b11);
        //@formatter:on

        public final int val;

        FmtEnum(int i) {
            this.val = i;
        }

        public static FmtEnum valueOf(int type) {
            FmtEnum fmt;
            switch (type) {
                case 0 -> fmt = FMT_00;
                case 1 -> fmt = FMT_01;
                case 2 -> fmt = FMT_10;
                case 3 -> fmt = FMT_11;
                default -> throw new IllegalArgumentException("非法的 fmt: " + type);
            }
            return fmt;
        }
    }

    //@formatter:off

    private static final int MAX_TIMESTAMP = 0xffffff;
    private static final int AV_DATA_CSID = 7;
    private int inboundChunkSize = 128;
    /** mtu */
    private int outboundChunkSize = 128;
    private DecodeState state = DecodeState.READ_HEADER;
    /** 当前正在处理的 csid */
    private int currentCsid;
    private final Map<Integer, ChunkMessage> chunkCache = new HashMap<>();
    private boolean isFistAudioDataPush = true, isFistVideoDataPush = true, isFirstAVDataPush = true;
    private int preAVTimestamp;

    //@formatter:on

    @Override
    protected void encode(ChannelHandlerContext ctx, RtmpMessage msg, ByteBuf out) {
        final var payload = msg.payload().duplicate();
        final var messageType = msg.messageType();
        final var streamId = msg.streamId();

        // rtmp message -> chunk message
        var fmt = FmtEnum.FMT_00;
        int csid;
        int ts = (int) msg.timestamp();
        switch (messageType) {
            case WINDOW_ACKNOWLEDGEMENT_SIZE, SET_PEER_BANDWIDTH, AMF0_COMMAND -> csid = 2;
            case SET_CHUNK_SIZE -> {
                csid = 2;
                // chunk size change
                payload.markReaderIndex();
                var chunkSize = payload.readInt();
                payload.resetReaderIndex();
                this.outboundChunkSize = chunkSize;
            }
            case AMF0_DATA -> csid = 5;
            case AUDIO_DATA -> {
                csid = AV_DATA_CSID;
                fmt = FmtEnum.FMT_01;
                if (isFistAudioDataPush) {
                    // + stream id
                    fmt = FmtEnum.FMT_00;
                    isFistAudioDataPush = false;

                    // 时间戳记录
                    if (isFirstAVDataPush) {
                        isFirstAVDataPush = false;
                        this.preAVTimestamp = ts;
                    }
                } else {
                    // timestamp delta
                    var delta = ts - preAVTimestamp;
                    preAVTimestamp = ts;
                    ts = delta;
                }
            }
            case VIDEO_DATA -> {
                csid = AV_DATA_CSID;
                fmt = FmtEnum.FMT_01;
                if (isFistVideoDataPush) {
                    // + stream id
                    fmt = FmtEnum.FMT_00;
                    isFistVideoDataPush = false;

                    // 时间戳记录
                    if (isFirstAVDataPush) {
                        isFirstAVDataPush = false;
                        this.preAVTimestamp = ts;
                    }
                } else {
                    // timestamp delta
                    var delta = ts - preAVTimestamp;
                    preAVTimestamp = ts;
                    ts = delta;
                }
            }
            default -> throw new StreamxException("不支持的消息类型: " + messageType);
        }

        // fmt + csid 处理
        out.writeByte((fmt.val << 6) + csid);
        // 时间戳处理
        out.writeMedium(Math.min(ts, MAX_TIMESTAMP));
        // body size 处理
        out.writeMedium(payload.readableBytes());
        // type id 处理
        out.writeByte(messageType.val);
        // stream id 处理
        if (fmt == FmtEnum.FMT_00) {
            out.writeIntLE(streamId);
        }
        // 拓展时间戳处理
        if (ts > MAX_TIMESTAMP) {
            out.writeInt(ts);
        }

        multiplexing(csid, payload, out);
    }

    private void multiplexing(int csid, ByteBuf payload, ByteBuf dst) {
        // chunk message 处理
        if (payload.readableBytes() < this.outboundChunkSize) {
            // 消息不需要分块
            dst.writeBytes(payload);
        } else {
            // 消息需要分块
            // 1 先处理当前 chunk
            dst.writeBytes(payload, this.outboundChunkSize);

            // 2 剩余的报文写入(fmt 3)
            do {
                var latestWriteLen = Math.min(payload.readableBytes(), this.outboundChunkSize);
                dst
                        .writeByte((FmtEnum.FMT_11.val << 6) + csid)
                        .writeBytes(payload, latestWriteLen);
            }
            while (payload.isReadable());
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state) {
            case READ_HEADER -> decodeChunkHeader(in);
            case READ_BODY -> {
                var msg = decodeChunkBody(in);
                if (msg == null) {
                    return;
                }

                // 处理 set chunk size
                if (RtmpMessageType.SET_CHUNK_SIZE == msg.messageType()) {
                    // Protocol control message 1, Set Chunk Size, is used to notify the
                    // peer of a new maximum chunk size.
                    //
                    // This field holds the new maximum chunk size,
                    // in bytes, which will be used for all of the sender’s subsequent
                    // chunks until further notice. Valid sizes are 1 to 2147483647
                    // (0x7FFFFFFF) inclusive; however, all sizes greater than 16777215
                    // (0xFFFFFF) are equivalent since no chunk is larger than one
                    // message, and no message is larger than 16777215 bytes.

                    int chunkSize = msg.payload().readInt();
                    if (chunkSize >>> 31 != 0) {
                        throw new IllegalArgumentException("非法的 chunk size: " + chunkSize);
                    }
                    inboundChunkSize = chunkSize;
                }

                out.add(msg);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        // release chunk message
        chunkCache.values().forEach(ChunkMessage::release);

        ctx.fireChannelInactive();
    }

    private void decodeChunkHeader(ByteBuf in) {
        var bufLen = in.readableBytes();
        in.markReaderIndex();

        // 1 basic header
        // fmt, csid
        var headLen = 1;
        byte b = in.readByte();
        var fmt = (b & 0xff) >> 6;
        var csid = b & 0x3f;
        switch (csid) {
            case 0 -> {
                if (bufLen < 2) {
                    in.resetReaderIndex();
                    return;
                }
                csid = (in.readByte() & 0xff) + 64;
                headLen = 2;
            }
            case 1 -> {
                if (bufLen < 3) {
                    in.resetReaderIndex();
                    return;
                }
                csid = in.readUnsignedShortLE() + 64;
                headLen = 3;
            }
        }

        // csid set
        this.currentCsid = csid;

        // 2 message header
        var preChunk = chunkCache.get(csid);
        switch (FmtEnum.valueOf(fmt)) {
            case FMT_00 -> {
                if (bufLen < headLen + 11) {
                    in.resetReaderIndex();
                    return;
                }

                // 时间戳
                long ts = in.readUnsignedMedium();
                // body size
                var bodySize = in.readUnsignedMedium();
                // type id
                var typeId = in.readByte() & 0xff;
                // stream id
                var streamId = in.readIntLE();

                // 拓展时间戳检查
                if (ts == 0xffffff) {
                    if (bufLen < headLen + 11 + 4) {
                        in.resetReaderIndex();
                        return;
                    }
                    ts = in.readUnsignedInt();
                }

                // 将当前 header 置入 cache
                chunkCache.put(csid, new ChunkMessage(fmt, csid, ts, bodySize, typeId, streamId));
            }
            case FMT_01 -> {
                // Type 1 chunk headers are 7 bytes long. The message stream ID is not
                // included; this chunk takes the same stream ID as the preceding chunk.
                // Streams with variable-sized messages (for example, many video
                // formats) SHOULD use this format for the first chunk of each new
                // message after the first.
                if (bufLen < headLen + 7) {
                    in.resetReaderIndex();
                    return;
                }

                // 时间戳增量
                long tsDelta = in.readUnsignedMedium();
                // body size
                var bodySize = in.readUnsignedMedium();
                // type id
                var typeId = in.readByte() & 0xff;

                // 拓展时间戳检查
                if (tsDelta == 0xffffff) {
                    if (bufLen < headLen + 7 + 4) {
                        in.resetReaderIndex();
                        return;
                    }
                    tsDelta = in.readUnsignedInt();
                }

                // 变更 type 1 的数据
                preChunk.setFmt(fmt).setTsDelta(tsDelta).setBodySize(bodySize)
                        .setTypeId(typeId)
                        .refreshBodyCapacity();
            }
            case FMT_10 -> {
                // Type 2 chunk headers are 3 bytes long. Neither the stream ID nor the
                // message length is included; this chunk has the same stream ID and
                // message length as the preceding chunk. Streams with constant-sized
                // messages (for example, some audio and data formats) SHOULD use this
                // format for the first chunk of each message after the first.
                if (bufLen < headLen + 3) {
                    in.resetReaderIndex();
                    return;
                }

                // 时间戳增量
                long tsDelta = in.readUnsignedMedium();

                // 拓展时间戳检查
                if (tsDelta == 0xffffff) {
                    if (bufLen < headLen + 3 + 4) {
                        in.resetReaderIndex();
                        return;
                    }
                    tsDelta = in.readUnsignedInt();
                }

                // 直接变更 pre chunk 的 ts, exTs
                preChunk.setFmt(fmt).setTsDelta(tsDelta).refreshBodyCapacity();
            }
            case FMT_11 -> {
                // Type 3 chunks have no message header. The stream ID, message length
                // and timestamp delta fields are not present; chunks of this type take
                // values from the preceding chunk for the same Chunk Stream ID. When a
                // single message is split into chunks, all chunks of a message except
                // the first one SHOULD use this type. Refer to Example 2
                // (Section 5.3.2.2). A stream consisting of messages of exactly the
                // same size, stream ID and spacing in time SHOULD use this type for all
                // chunks after a chunk of Type 2. Refer to Example 1
                // (Section 5.3.2.1). If the delta between the first message and the
                // second message is same as the timestamp of the first message, then a
                // chunk of Type 3 could immediately follow the chunk of Type 0 as there
                // is no need for a chunk of Type 2 to register the delta. If a Type 3
                // chunk follows a Type 0 chunk, then the timestamp delta for this Type
                // 3 chunk is the same as the timestamp of the Type 0 chunk.

                // 时间戳兼容处理
                // 实现存在异议，引用 nginx-rtmp 的说法:
                // Messages with type=3 should
                // never have ext timestamp field
                // according to standard.
                // However that's not always the case
                // in real life
                if (bufLen < headLen + 4) {
                    in.resetReaderIndex();
                    return;
                }
                in.markReaderIndex();
                switch (preChunk.fmt) {
                    case 0 -> {
                        var currentTs = in.readUnsignedInt();
                        // 判断 fmt 3 base header 后面是否是拓展时间戳
                        if (!(preChunk.ts > MAX_TIMESTAMP && currentTs == preChunk.ts)) {
                            in.resetReaderIndex();
                        }
                    }
                    case 1, 2 -> {
                        var currentTs = in.readUnsignedInt();
                        // 判断 fmt 3 base header 后面是否是拓展时间增量
                        if (!(preChunk.tsDelta > MAX_TIMESTAMP && currentTs == preChunk.tsDelta)) {
                            in.resetReaderIndex();
                        }
                    }
                }
            }
        }

        // 切换解码状态
        this.state = DecodeState.READ_BODY;
    }

    private RtmpMessage decodeChunkBody(ByteBuf in) {
        // 计算出本次解码需要读取的 size
        var chunk = chunkCache.get(currentCsid);
        var writableBytes = chunk.body.writableBytes();
        var readSize = Math.min(inboundChunkSize, writableBytes);

        // 数据读取
        var bufLen = in.readableBytes();
        if (bufLen < readSize) {
            return null;
        }
        in.readBytes(chunk.body, readSize);

        // 新的 chunk header 读取
        this.state = DecodeState.READ_HEADER;

        // 检查 chunk 是否完整
        if (chunk.isComplete()) {
            return chunk.toRtmpMessage();
        }
        return null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error(cause.getMessage(), cause);
        ctx.close();
    }

    private static class ChunkMessage {
        //@formatter:off

        public int fmt;
        /** chunk stream id */
        public int csid;
        /** 这里的 ts 应该是 calculation timestamp */
        public long ts;
        public int bodySize;
        public int typeId;
        public int streamId;
        public long tsDelta;

        public ByteBuf body;

        //@formatter:on


        public ChunkMessage(int fmt, int csid, long ts, int bodySize, int typeId, int streamId) {
            this(fmt, csid, ts, bodySize, typeId, streamId, Unpooled.buffer(bodySize));
        }


        public ChunkMessage(int fmt, int csid, long ts, int bodySize, int typeId, int streamId, ByteBuf body) {
            this.fmt = fmt;
            this.csid = csid;
            this.ts = ts;
            this.bodySize = bodySize;
            this.typeId = typeId;
            this.streamId = streamId;
            this.body = body;
        }

        public RtmpMessage toRtmpMessage() {
            var msg = new RtmpMessage(
                    RtmpMessageType.valueOf(this.typeId),
                    ts,
                    this.streamId,
                    body.copy()
            );
            body.clear();
            return msg;
        }


        public void release() {
            this.body.release();
        }

        public ChunkMessage setFmt(int fmt) {
            this.fmt = fmt;
            return this;
        }

        public ChunkMessage setTsDelta(long tsDelta) {
            this.tsDelta = tsDelta;
            this.ts += tsDelta;
            return this;
        }

        /**
         * 设置 chunk 的 body size
         *
         * @param bodySize chunk body size
         */
        public ChunkMessage setBodySize(int bodySize) {
            this.bodySize = bodySize;
            return this;
        }

        public void refreshBodyCapacity() {
            // 更新一下 body size
            this.body.capacity(bodySize);
        }

        public ChunkMessage setTypeId(int typeId) {
            this.typeId = typeId;
            return this;
        }

        public boolean isComplete() {
            return !this.body.isWritable();
        }

        @Override
        public String toString() {
            return "ChunkMessage{" +
                    "fmt=" + fmt +
                    ", csid=" + csid +
                    ", ts=" + ts +
                    ", bodySize=" + bodySize +
                    ", typeId=" + typeId +
                    ", streamId=" + streamId +
                    ", body=" + body +
                    '}';
        }
    }
}
