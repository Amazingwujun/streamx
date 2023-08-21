package com.jun.streamx.commons.utils;

import com.jun.streamx.commons.Nullable;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.Map;

/**
 * spring 上下文相关工具
 *
 * @author Jun
 * @since 1.0.0
 */
@Component
@ConditionalOnClass(ApplicationEvent.class)
public class SpringContextUtils {

    private static ApplicationContext context;
    private static String applicationName;

    public SpringContextUtils(ApplicationContext context) {
        Assert.notNull(context, "ApplicationContext can't be null");

        SpringContextUtils.context = context;
    }

    /**
     * 返回当前服务名称
     *
     * @return spring.application.name
     */
    public static String applicationName() {
        if (applicationName == null) {
            synchronized (SpringContextUtils.class) {
                if (applicationName == null) {
                    applicationName = context.getEnvironment().getProperty("spring.application.name");
                }
            }
        }
        return applicationName;
    }

    /**
     * @see ApplicationContext#getBean(Class)
     */
    public static <T> T getBean(Class<T> clazz) {
        return context.getBean(clazz);
    }

    /**
     * @see ApplicationContext#getBeansOfType(Class)
     */
    public static <T> Map<String, T> getBeansOfType(Class<T> clazz) {
        return context.getBeansOfType(clazz);
    }

    /**
     * @see ApplicationContext#publishEvent(ApplicationEvent)
     */
    public static void publish(ApplicationEvent event) {
        context.publishEvent(event);
    }

    /**
     * @see ApplicationContext#publishEvent(Object)
     */
    public static void publish(Object event) {
        context.publishEvent(event);
    }

    /**
     * 返回 {@link ApplicationContext} 引用
     */
    @Nullable
    public static ApplicationContext context(){
        return context;
    }
}
