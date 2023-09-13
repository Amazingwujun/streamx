package com.jun.streamx.broker;

import com.jun.streamx.commons.utils.ByteUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * flv 协议解码器.
 * <p>
 *
 * @author Jun
 * @since 1.0.0
 */
@Slf4j
public class FlvDecoder extends ByteToMessageDecoder {

    private enum DecodeState {
        READ_HEAD,
        READ_SCRIPT_TAG_HEAD,
        READ_SCRIPT_TAG_DATA,
        READ_AV_TAG
    }

    private DecodeState state = DecodeState.READ_HEAD;
    private final byte[] flvHeaders = new byte[9];
    private final byte[] scriptTagHeader = new byte[4 + 11];
    private byte[] scriptTagData;
    public static byte[] headers;
    private int scriptTagDataLen;
    private final byte[] headBuf = new byte[11];
    private boolean isFirstAvTag = true;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state) {
            case READ_HEAD -> {
                // 解析获取 flv header
                if (in.readableBytes() < 9) {
                    return;
                }
                in.readBytes(flvHeaders);

                // Signature 检查
                if (flvHeaders[0] == 0x46 &&
                        flvHeaders[1] == 0x4c &&
                        flvHeaders[2] == 0x56) {
                    state = DecodeState.READ_SCRIPT_TAG_HEAD;
                } else {
                    log.error("flv header check failed: [{}]", ByteUtils.toHexString(" ", flvHeaders));
                }
            }
            case READ_SCRIPT_TAG_HEAD -> {
                // pre tag size0 + script tag head
                if (in.readableBytes() < 4 + 11) {
                    return;
                }

                // 读取 header
                in.readBytes(scriptTagHeader);

                if (scriptTagHeader[4] != 0x12) {
                    log.error("check script tag failed");
                    return;
                }

                // 数据域长度计算
                this.scriptTagDataLen =
                        ((scriptTagHeader[5] & 0xff) << 16) +
                                ((scriptTagHeader[6] & 0xff) << 8) +
                                (scriptTagHeader[7] & 0xff);

                this.state = DecodeState.READ_SCRIPT_TAG_DATA;
            }
            case READ_SCRIPT_TAG_DATA -> {
                if (in.readableBytes() < scriptTagDataLen + 4) {
                    return;
                }

                this.scriptTagData = new byte[scriptTagDataLen + 4];
                in.readBytes(scriptTagData);

                this.state = DecodeState.READ_AV_TAG;
            }
            case READ_AV_TAG -> {
                if (in.readableBytes() < 11) {
                    return;
                }

                in.markReaderIndex();
                in.readBytes(headBuf);

                var b0 = headBuf[1];
                var b1 = headBuf[2];
                var b2 = headBuf[3];
                int tagLen = ((b0 & 0xff) << 16) + ((b1 & 0xff) << 8) + (b2 & 0xff);

                if (in.readableBytes() < tagLen + 4) {
                    in.resetReaderIndex();
                    return;
                }

                // 记录下可用的 flv header
                if (isFirstAvTag) {
                    isFirstAvTag = false;

                    var buf = new byte[tagLen + 4];
                    in.readBytes(buf);
                    headers = ByteUtils.byteMerge(
                            flvHeaders,
                            scriptTagHeader,
                            scriptTagData,
                            headBuf,
                            buf
                    );
                } else {
                    in.resetReaderIndex();
                    var buf = new byte[11 + tagLen + 4];
                    in.readBytes(buf);

                    int t = ((buf[7] & 0xff) << 24) + ((buf[4] & 0xff) << 16) + ((buf[5] & 0xff) << 8) + buf[6];
                    log.info("tag: {}:{}:{}", headBuf[0], t, Integer.toHexString(buf[11]));
                }
            }
        }
    }
}
