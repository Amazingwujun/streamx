package com.jun.streamx.broker;

import com.jun.streamx.broker.config.BizProperties;
import com.jun.streamx.commons.StreamxApp;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(BizProperties.class)
@SpringBootApplication
public class BrokerApplication {

    public static void main(String[] args) {
        StreamxApp.run(BrokerApplication.class, args);
    }

}
