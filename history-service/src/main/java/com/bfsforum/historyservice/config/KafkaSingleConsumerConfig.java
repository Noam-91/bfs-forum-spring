package com.bfsforum.historyservice.config;

import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.service.RequestReplyManager;
import com.bfsforum.historyservice.service.HistoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Configuration for single-post notification consumer.
 */
@Configuration
@Slf4j
public class KafkaSingleConsumerConfig {

    @Bean
    public Consumer<Message<Post>> postNotificationEventConsumer(
            HistoryService historyService
    ) {
        return message -> {
            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
            Post post = message.getPayload();
            log.info("Received post notification for correlationId {}: {}", correlationId, post);

            // Persist the view record
            historyService.recordView(post.getUserId(), post.getId());
        };
    }
}
