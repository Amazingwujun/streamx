package com.jun.streamx.broker.codec;

import com.jun.streamx.commons.utils.Time;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.bytedeco.libfreenect._freenect_context;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * rtmp 简单握手协议处理器
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class RtmpSimpleHandshakeHandler extends ByteToMessageDecoder {

    private enum DecodeState {
        C0_C1,
        S0_S1_S2,
        C2
    }

    private DecodeState state = DecodeState.C0_C1;

    /**
     * 协议解码.
     * <p>
     * client [c0,c1] -> server
     * server [s0,s1,s2] -> client
     * client [c2] -> server
     *
     * @param ctx the {@link ChannelHandlerContext} which this {@link ByteToMessageDecoder} belongs to
     * @param in  the {@link ByteBuf} from which to read data
     * @param out the {@link List} to which decoded messages should be added
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state) {
            case C0_C1 -> {
                if (in.readableBytes() < 1 + 1536) {
                    return;
                }

                // 版本号检查
                var ver = in.readByte();
                if (ver != 3) {
                    log.warn("不支持的版本号: {}", ver);
                }

                // S0, S1, S2 写入
                // S0
                var buf = Unpooled.buffer(1 + 1536 + 1536);
                buf.writeByte(3);
                // S1
                var ts = (int) (System.currentTimeMillis() / 1000);
                buf.writeInt(ts); // 时间戳写入
                buf.writeInt(0); // 0 值写入
                var rd = ThreadLocalRandom.current();
                for (int i = 0; i < 1536 - 8; i++) {
                    var randomByte = rd.nextInt(Byte.MIN_VALUE, Byte.MAX_VALUE);
                    buf.writeByte(randomByte);
                }
                // S2
                var c1Ts = in.readInt(); // 读取 c1 时间戳
                buf.writeInt(c1Ts); // time 写入
                buf.writeInt(ts); // time2 写入
                in.skipBytes(4); // 跳过 zero
                buf.writeBytes(in, 1536 - 8);
                ctx.writeAndFlush(buf);

                state = DecodeState.C2;
            }
            case S0_S1_S2 -> {
                // todo 客户端解码
                if (in.readableBytes() < 1 + 1536 + 1536) {
                    return;
                }
            }
            case C2 -> {
                if (in.readableBytes() < 1536) {
                    return;
                }

                // 读取时间戳
                var c1Ts = in.readInt();
                var s1Ts = in.readInt();
                in.skipBytes(1536 - 8);

                log.debug("握手完成, c1_ts[{}], s1_ts[{}]", Time.ofEpochSecond(c1Ts), Time.ofEpochSecond(s1Ts));

                // 移除握手处理器
                ctx.pipeline().remove(RtmpSimpleHandshakeHandler.class);
            }
        }
    }
}
