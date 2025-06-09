package com.bfsforum.historyservice.config;

import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.service.RequestReplyManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import java.util.List;
import java.util.function.Consumer;

/**
 * Configuration for list-of-post enrichment reply consumer.
 */
@Configuration
@Slf4j
public class KafkaListConsumerConfig {

    @Bean("postListManager")
    public RequestReplyManager<List<Post>> postListManager() {
        return new RequestReplyManager<>();
    }

    @Bean
    public Consumer<Message<List<Post>>> postEnrichRequestEventConsumer(
            @Qualifier("postListManager") RequestReplyManager<List<Post>> postListManager
    ) {
        return message -> {
            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
            List<Post> posts = message.getPayload();
            log.info("Received post list reply for correlationId {}: {}", correlationId, posts);

            // Complete the future
            postListManager.completeFuture(correlationId, posts);
        };
    }
}
