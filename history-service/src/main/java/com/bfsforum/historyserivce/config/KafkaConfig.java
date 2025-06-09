package com.bfsforum.historyserivce.config;

import com.bfsforum.historyserivce.kafka.event.PostsEnrichmentRequest;
import com.bfsforum.historyserivce.kafka.event.PostsEnrichmentResponse;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic enrichmentRequestTopic() {
        return TopicBuilder.name("posts-enrichment-request")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic enrichmentReplyTopic() {
        return TopicBuilder.name("posts-enrichment-response")
                .partitions(1).replicas(1).build();
    }

    @Bean
    public ReplyingKafkaTemplate<String, PostsEnrichmentRequest, PostsEnrichmentResponse>
    replyingKafkaTemplate(ProducerFactory<String, PostsEnrichmentRequest> pf,
                          ConcurrentMessageListenerContainer<String, PostsEnrichmentResponse> repliesContainer) {
        return new ReplyingKafkaTemplate<>(pf, repliesContainer);
    }

    @Bean
    public ConcurrentMessageListenerContainer<String, PostsEnrichmentResponse> repliesContainer(
            ConsumerFactory<String, PostsEnrichmentResponse> cf) {
        ContainerProperties props = new ContainerProperties("posts-enrichment-response");
        return new ConcurrentMessageListenerContainer<>(cf, props);
    }
}
