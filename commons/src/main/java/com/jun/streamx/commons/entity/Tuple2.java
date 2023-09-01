package com.jun.streamx.commons.entity;

/**
 * 二元组
 *
 * @param <T0>
 * @param <T1>
 * @author Jun
 * @since 1.1.0
 */
public record Tuple2<T0, T1>(T0 t0, T1 t1) {
}
