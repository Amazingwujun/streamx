package com.jun.streamx.commons.utils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 时间相关工具类
 *
 * @author Jun
 * @since 1.0.0
 */
public class Time {

    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static final String DATE_FORMAT = "yyyy-MM-dd";

    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 时区-北京时间
     */
    public static final ZoneOffset BEI_JING = ZoneOffset.of("+8");

    /**
     * 常用时间格式化类
     */
    public static final DateTimeFormatter COMMON_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);
    public static final DateTimeFormatter COMMON_TIME_FORMATTER = DateTimeFormatter.ofPattern(TIME_FORMAT);
    public static final DateTimeFormatter COMMON_DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private static final Map<String, DateTimeFormatter> CACHE = new ConcurrentHashMap<>();

    static {
        CACHE.put(DATE_TIME_FORMAT, COMMON_DATE_TIME_FORMATTER);
        CACHE.put(TIME_FORMAT, COMMON_TIME_FORMATTER);
        CACHE.put(DATE_FORMAT, COMMON_DATE_FORMATTER);
    }

    /**
     * 将 {@link LocalDateTime} 转为 {@link String}, 格式：{@linkplain #DATE_TIME_FORMAT}
     *
     * @param target 待格式化对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime target) {
        return target.format(COMMON_DATE_TIME_FORMATTER);
    }

    /**
     * 将 {@link LocalDate} 转为 {@link String}, 格式：{@linkplain #DATE_FORMAT}
     *
     * @param target 待格式化对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDate target) {
        return target.format(COMMON_DATE_FORMATTER);
    }

    /**
     * 将 {@link LocalTime} 转为 {@link String}, 格式：{@linkplain #TIME_FORMAT}
     *
     * @param target 待格式化对象
     * @return 格式化后的字符串
     */
    public static String format(LocalTime target) {
        return target.format(COMMON_TIME_FORMATTER);
    }

    /**
     * 将 {@link LocalDateTime} 按指定格式 {@code pattern} 转为 {@link String}.
     *
     * @param target  待格式化对象
     * @param pattern 指定格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime target, String pattern) {
        var formatter = CACHE.computeIfAbsent(pattern, k -> DateTimeFormatter.ofPattern(pattern));
        return target.format(formatter);
    }

    /**
     * 将 {@link LocalDate} 按指定格式 {@code pattern} 转为 {@link String}.
     *
     * @param target  待格式化对象
     * @param pattern 指定格式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate target, String pattern) {
        var formatter = CACHE.computeIfAbsent(pattern, k -> DateTimeFormatter.ofPattern(pattern));
        return target.format(formatter);
    }

    /**
     * 将 {@link LocalTime} 按指定格式 {@code pattern} 转为 {@link String}.
     *
     * @param target  待格式化对象
     * @param pattern 指定格式
     * @return 格式化后的字符串
     */
    public static String format(LocalTime target, String pattern) {
        var formatter = CACHE.computeIfAbsent(pattern, k -> DateTimeFormatter.ofPattern(pattern));
        return target.format(formatter);
    }

    /**
     * 由毫秒级时间戳转为 {@link LocalDateTime}, 时区为 {@link #BEI_JING}.
     *
     * @param epochMilli 毫秒时间戳
     * @return {@link LocalDateTime}
     * @see Instant#ofEpochMilli(long)
     */
    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), BEI_JING);
    }

    /**
     * 由秒级时间戳转为 {@link LocalDateTime}, 时区为 {@link #BEI_JING}.
     *
     * @param epochSecond 秒时间戳
     * @return {@link LocalDateTime}
     * @see Instant#ofEpochMilli(long)
     */
    public static LocalDateTime ofEpochSecond(long epochSecond) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochSecond * 1000), BEI_JING);
    }
}
