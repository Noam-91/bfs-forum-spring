package com.bfsforum.fileservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.US_EAST_2) // ✅ Your region here
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create("AKIAQ54B6CQQE6AJQIEJ", "jG/BGiN8OBHPqrZaslUsd+B+Y86d1fsLq5aolt+z")
            ))
            .build();
    }
}