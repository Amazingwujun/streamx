package com.jun.streamx.commons.entity;

/**
 * 五元组
 *
 * @param t0
 * @param t1
 * @param t2
 * @param t3
 * @param t4
 * @param <T0>
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 * @author Jun
 * @since 1.0.0
 */
public record Tuple5<T0, T1, T2, T3, T4>(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4) {
}
