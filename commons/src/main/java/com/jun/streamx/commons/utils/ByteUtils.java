package com.jun.streamx.commons.utils;

import java.nio.ByteOrder;

/**
 * 字节操作相关工具类
 *
 * @author Jun
 * @since 1.0.0
 */
public class ByteUtils {

    private static final char[] HEXES = {
            '0', '1', '2', '3',
            '4', '5', '6', '7',
            '8', '9', 'a', 'b',
            'e', 'd', 'e', 'f'
    };

    /**
     * 用于计算数据帧的bcc码
     *
     * @param target 给定的字节数组
     */
    public static byte bccCalculate(byte[] target) {
        byte bcc = target[0];
        for (int i = 1; i < target.length; i++) {
            bcc ^= target[i];
        }

        return bcc;
    }

    /**
     * 将 long 转为 8 字节数组
     *
     * @param data      待转换目标
     * @param byteOrder 字节序
     */
    public static byte[] long2bytes(long data, ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder 不能为空");
        }

        var bytes = new byte[8];
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((data >> ((7 - i) << 3)) & 0xff);
            }
        } else {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((data >> (i << 3)) & 0xff);
            }
        }

        return bytes;
    }

    /**
     * 将 int 转为 4 字节数组
     *
     * @param data      待转换目标
     * @param byteOrder 字节序
     */
    public static byte[] int2bytes(int data, ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder 不能为空");
        }

        var bytes = new byte[4];
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((data >> ((3 - i) << 3)) & 0xff);
            }
        } else {
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = (byte) ((data >> (i << 3)) & 0xff);
            }
        }

        return bytes;
    }

    /**
     * 将 short 转为 2 字节数组
     *
     * @param data      待转换目标
     * @param byteOrder 字节序
     */
    public static byte[] short2bytes(short data, ByteOrder byteOrder) {
        if (byteOrder == null) {
            throw new IllegalArgumentException("byteOrder 不能为空");
        }

        final var len = 2;
        var bytes = new byte[len];
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < len; i++) {
                bytes[i] = (byte) ((data >> ((1 - i) << 3)) & 0xff);
            }
        } else {
            for (int i = 0; i < len; i++) {
                bytes[i] = (byte) ((data >> (i << 3)) & 0xff);
            }
        }

        return bytes;
    }

    /**
     * 将 <strong>2</strong> 字节数组转为 short(signed).
     *
     * @param data      两字节数组
     * @param byteOrder 字节序, {@link ByteOrder}
     */
    public static short bytes2short(byte[] data, ByteOrder byteOrder) {
        if (data == null || data.length != 2) {
            throw new IllegalArgumentException("字节数组不能为空或数据长度非法");
        }

        short result = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            result |= (data[0] & 0xff) << 8;
            result |= data[1] & 0xff;
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            result |= data[0] & 0xff;
            result |= (data[1] & 0xff) << 8;
        } else {
            throw new IllegalArgumentException(String.format("非法的字节序: %s", byteOrder));
        }

        return result;
    }

    /**
     * 将 <strong>2</strong> 字节数组转为 short(unsigned).
     *
     * @param data      两字节数组
     * @param byteOrder 字节序, {@link ByteOrder}
     */
    public static int bytes2unsignedShort(byte[] data, ByteOrder byteOrder) {
        return bytes2short(data, byteOrder) & 0xFFFF;
    }

    /**
     * 将 <strong>4</strong> 字节数组转为 int(signed).
     *
     * @param data      四字节数组
     * @param byteOrder 字节序, {@link ByteOrder}
     */
    public static int bytes2int(byte[] data, ByteOrder byteOrder) {
        if (data == null || data.length != 4) {
            throw new IllegalArgumentException("字节数组不能为空或数据长度非法");
        }

        int result = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 4; i++) {
                result |= (data[i] & 0xff) << ((3 - i) << 3);
            }
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0; i < 4; i++) {
                result |= (data[i] & 0xff) << (i << 3);
            }
        } else {
            throw new IllegalArgumentException(String.format("非法的字节序: %s", byteOrder));
        }


        return result;
    }

    /**
     * 将 <strong>4</strong> 字节数组转为 long(unsigned).
     *
     * @param data      四字节数组
     * @param byteOrder 字节序, {@link ByteOrder}
     */
    public static long bytes2unsignedInt(byte[] data, ByteOrder byteOrder) {
        return bytes2int(data, byteOrder) & 0xFFFFFFFFL;
    }

    /**
     * 将 <strong>8</strong> 字节数组转为 long(signed)
     *
     * @param data      八字节数组
     * @param byteOrder 字节序
     */
    public static long bytes2long(byte[] data, ByteOrder byteOrder) {
        if (data == null || data.length != 8) {
            throw new IllegalArgumentException("字节数组不能为空或数据长度非法");
        }

        long result = 0;
        if (byteOrder == ByteOrder.BIG_ENDIAN) {
            for (int i = 0; i < 8; i++) {
                result |= (data[i] & 0xffL) << ((7 - i) << 3);
            }
        } else if (byteOrder == ByteOrder.LITTLE_ENDIAN) {
            for (int i = 0; i < 8; i++) {
                result |= (data[i] & 0xffL) << (i << 3);
            }
        } else {
            throw new IllegalArgumentException(String.format("非法的字节序: %s", byteOrder));
        }

        return result;
    }

    /**
     * 计算CRC16校验码
     *
     * @param bytes 需要计算 crc 的数据
     * @return crc 数据
     */
    public static byte[] crcModbus(byte[] bytes) {
        int CRC = 0xffff;
        int POLYNOMIAL = 0xa001;

        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= (bytes[i] & 0xff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x1) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }

        byte[] crc = new byte[2];
        crc[0] = (byte) (CRC & 0xff);
        crc[1] = (byte) ((CRC >> 8) & 0xff);
        return crc;
    }

    public static void main(String[] args) {
        System.out.println(toHexString(" ", "nani".getBytes()));
    }

    /**
     * 将字节数组装换为 16 进制的字符串
     *
     * @param source 字节数组
     * @return 16 进制的字符串
     */
    public static String toHexString(String splitter, byte... source) {
        if (source == null) return null;
        var sb = new StringBuilder();
        for (int i = 0; i < source.length; i++) {
            var b = source[i];
            char h = HEXES[(b >> 4) & 0xf];
            char l = HEXES[b & 0xf];
            sb.append(h).append(l);
            if (i < source.length - 1) {
                sb.append(splitter);
            }
        }

        return sb.toString();
    }

    /**
     * 将 hex 格式的字符串转为字节数组
     *
     * @param hex 16进制字符串
     * @return 字节数组
     */
    public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length() / 2;
        byte[] buf = new byte[len];
        for (int i = 0; i < len; i++) {
            buf[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }

        return buf;
    }

    /**
     * 多字节数组合并
     *
     * @param values 需要合并的自己数组
     * @return 合并后的字节数组
     */
    public static byte[] byteMerge(final byte[]... values) {
        var length = values.length;
        switch (length) {
            case 0 -> {
                return new byte[]{};
            }
            case 1 -> {
                return values[0];
            }
            default -> {
                var len = 0;
                for (byte[] value : values) {
                    len += value.length;
                }

                var result = new byte[len];

                var destPos = 0;
                for (var value : values) {
                    System.arraycopy(value, 0, result, destPos, value.length);
                    destPos += value.length;
                }

                return result;
            }
        }
    }
}
