package com.bfsforum.userservice.config;

import com.bfsforum.userservice.dto.EmailVerificationReply;
import com.bfsforum.userservice.service.RequestReplyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;

import java.util.function.Consumer;

@Slf4j
@Configuration
public class KafkaConsumerConfig {

    private final RequestReplyManager<EmailVerificationReply> requestReplyManager;

    public KafkaConsumerConfig(RequestReplyManager<EmailVerificationReply> requestReplyManager) {
        this.requestReplyManager = requestReplyManager;
    }

    @Bean
    public Consumer<Message<EmailVerificationReply>> emailVerificationReplyConsumer() {
        return message -> {
            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
            EmailVerificationReply reply = message.getPayload();

            log.info("Received token verification reply for correlationId {}: {}", correlationId, reply);
            requestReplyManager.completeFuture(correlationId, reply);
        };
    }
}