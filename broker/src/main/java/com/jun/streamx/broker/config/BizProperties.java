package com.jun.streamx.broker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 业务配置
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "streamx.broker")
public class BizProperties {
    //@formatter:off

    /** http,ws 端口 */
    private int port = 8988;
}
