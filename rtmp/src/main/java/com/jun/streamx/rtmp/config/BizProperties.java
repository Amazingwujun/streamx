package com.jun.streamx.rtmp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 业务配置
 *
 * @author Jun
 * @since 1.0.0
 */
@Data
@ConfigurationProperties(prefix = "streamx.rtmp")
public class BizProperties {
    //@formatter:off

    /** rtmp server port */
    private int port = 1935;
}
