package com.bfsforum.postservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import io.github.cdimascio.dotenv.Dotenv;

@EnableDiscoveryClient
@SpringBootApplication
public class PostServiceApplication {

    public static void main(String[] args) {
        // load .env
        Dotenv dotenv = Dotenv.configure()
                .directory("post-service")
                .filename(".env")
                .load();
        System.setProperty("MONGODB_URI", dotenv.get("MONGODB_URI"));
        SpringApplication.run(PostServiceApplication.class, args);
    }
}
