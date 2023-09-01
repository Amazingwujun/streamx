package com.jun.streamx.commons.entity;


/**
 * Tuple 对象生成工具类
 *
 * @author Jun
 * @since 1.0.0
 */
public class Tuples {

    /**
     * 创建一个 {@link Tuple2}
     *
     * @param t0   第一个元素
     * @param t1   第二个元素
     * @param <T0> 第一个元素的类型
     * @param <T1> 第二个元素的类型
     * @return new {@link Tuple2}
     */
    public static <T0, T1> Tuple2<T0, T1> of(T0 t0, T1 t1) {
        return new Tuple2<>(t0, t1);
    }

    /**
     * 创建一个 {@link Tuple3}
     *
     * @param t0   第一个元素
     * @param t1   第二个元素
     * @param t2   第三个元素
     * @param <T0> 第一个元素的类型
     * @param <T1> 第二个元素的类型
     * @param <T2> 第三个元素的类型
     * @return new {@link Tuple3}
     */
    public static <T0, T1, T2> Tuple3<T0, T1, T2> of(T0 t0, T1 t1, T2 t2) {
        return new Tuple3<>(t0, t1, t2);
    }

    /**
     * 创建一个 {@link Tuple4}
     *
     * @param t0   第一个元素
     * @param t1   第二个元素
     * @param t2   第三个元素
     * @param t3   第四个元素
     * @param <T0> 第一个元素的类型
     * @param <T1> 第二个元素的类型
     * @param <T2> 第三个元素的类型
     * @param <T3> 第四个元素的类型
     * @return new {@link Tuple4}
     */
    public static <T0, T1, T2, T3> Tuple4<T0, T1, T2, T3> of(T0 t0, T1 t1, T2 t2, T3 t3) {
        return new Tuple4<>(t0, t1, t2, t3);
    }

    /**
     * 创建一个 {@link Tuple5}
     *
     * @param t0   第一个元素
     * @param t1   第二个元素
     * @param t2   第三个元素
     * @param t3   第四个元素
     * @param t4   第五个元素
     * @param <T0> 第一个元素的类型
     * @param <T1> 第二个元素的类型
     * @param <T2> 第三个元素的类型
     * @param <T3> 第四个元素的类型
     * @param <T4> 第五个元素的类型
     * @return new {@link Tuple3}
     */
    public static <T0, T1, T2, T3, T4> Tuple5<T0, T1, T2, T3, T4> of(T0 t0, T1 t1, T2 t2, T3 t3, T4 t4) {
        return new Tuple5<>(t0, t1, t2, t3, t4);
    }
}
