package com.jun.streamx.broker;

import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
class BrokerApplicationTests {

    @Test
    void contextLoads() throws InterruptedException {
        System.out.println(new NET_DVR_PDC_ALRAM_INFO());
    }

    @ToString
    static class NET_DVR_PDC_ALRAM_INFO   {
        public int dwSize;
        public byte byMode;
        public byte byChannel;
        public byte bySmart;
        public byte byRes1;
        public int dwLeaveNum;
        public int dwEnterNum;
        public byte[] byRes2 = new byte[40];
    }
}
