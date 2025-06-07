package com.bfsforum.historyserivce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
@EnableCaching
public class HistorySerivceApplication {

    public static void main(String[] args) {
        SpringApplication.run(HistorySerivceApplication.class, args);
    }

}
