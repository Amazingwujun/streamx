package com.jun.streamx.broker;

import com.jun.streamx.commons.StreamxApp;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BrokerApplication {

    public static void main(String[] args) {
        StreamxApp.run(BrokerApplication.class, args);
    }

}
