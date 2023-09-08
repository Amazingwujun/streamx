package com.jun.streamx.rtmp;

import com.jun.streamx.rtmp.config.BizProperties;
import com.jun.streamx.commons.StreamxApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * RTMP 服务器
 *
 * @author Jun
 * @since 1.0.0
 */
@EnableConfigurationProperties(BizProperties.class)
@SpringBootApplication
public class RtmpApplication {

    public static void main(String[] args) {
        StreamxApp.run(RtmpApplication.class, args);
    }
}
