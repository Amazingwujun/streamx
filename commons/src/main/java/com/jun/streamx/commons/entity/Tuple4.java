package com.jun.streamx.commons.entity;

/**
 * 四元组
 *
 * @param t0
 * @param t1
 * @param t2
 * @param t3
 * @param <T0>
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @author Jun
 * @since 1.0.0
 */
public record Tuple4<T0, T1, T2, T3>(T0 t0, T1 t1, T2 t2, T3 t3) {
}
