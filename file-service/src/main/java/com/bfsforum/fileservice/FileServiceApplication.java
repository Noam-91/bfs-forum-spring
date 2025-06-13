package com.bfsforum.fileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
public class FileServiceApplication {

    public static void main(String[] args) {

        Dotenv dotenv = Dotenv.configure()
            .directory("file-service")
            .filename(".env")
            .load();

        dotenv.entries().forEach(entry -> {
            System.setProperty(entry.getKey(), entry.getValue());
            System.out.println("Loaded .env property: " + entry.getKey() + "=" + entry.getValue());
        });

        SpringApplication.run(FileServiceApplication.class, args);
    }
}