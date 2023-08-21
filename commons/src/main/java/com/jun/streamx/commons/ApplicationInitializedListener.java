package com.jun.streamx.commons;

/**
 * 应用启动完成事件监听器
 *
 * @author Jun
 * @since 1.0.0
 */
public interface ApplicationInitializedListener {

    /**
     * 当前监听器序号, 顺序参考 {@link org.springframework.core.Ordered}
     *
     * @return 当前监听器顺序
     */
    int order ();

    /**
     * 事件处理方法.
     * <p>
     * 注意：此方法请勿抛出异常.
     */
    void onAppInitialized();
}
