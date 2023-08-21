package com.jun.streamx.broker;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class BrokerApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        var f = new CompletableFuture<String>();
        f.orTimeout(3, TimeUnit.SECONDS);

        try {
            TimeUnit.SECONDS.sleep(4);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        f.complete("nani");
        f.whenComplete((r, t) -> {

            t.printStackTrace();
        });


        TimeUnit.HOURS.sleep(1);
    }

}
