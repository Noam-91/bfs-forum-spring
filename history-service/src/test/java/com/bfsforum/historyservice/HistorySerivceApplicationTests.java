package com.bfsforum.historyservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootTest
@EnableAspectJAutoProxy(exposeProxy = true)
class HistorySerivceApplicationTests {

    @Test
    void contextLoads() {
    }

}
