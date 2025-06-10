package com.bfsforum.historyservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableDiscoveryClient
@SpringBootApplication
@EnableCaching
//@EnableAspectJAutoProxy(exposeProxy = true)
public class HistorySerivceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistorySerivceApplication.class, args);
    }

}
