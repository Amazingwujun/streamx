package com.jun.streamx.commons.entity;

/**
 * 三元组
 *
 * @param <T0>
 * @param <T1>
 * @param <T2>
 * @author Jun
 * @since 1.1.0
 */
public record Tuple3<T0, T1, T2>(T0 t0, T1 t1, T2 t2) {
}
