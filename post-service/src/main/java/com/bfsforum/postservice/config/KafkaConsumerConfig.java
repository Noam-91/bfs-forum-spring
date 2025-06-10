package com.bfsforum.postservice.config;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import java.util.List;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class KafkaConsumerConfig {
  private final PostService postService;
  private final StreamBridge streamBridge;

  @Value("${app.kafka.topics.posts-enrichment-response}")
  private String postEnrichmentResponseBinding;

  public KafkaConsumerConfig(PostService postService, StreamBridge streamBridge) {
    this.postService = postService;
    this.streamBridge = streamBridge;
  }

  @Bean
  public Consumer<Message<List<String>>> postsEnrichmentConsumer() {
    return message -> {
      String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
      try {
        List<String> postIds = message.getPayload();
        List<Post> posts = postService.getBatchPostsById(postIds);

        streamBridge.send(postEnrichmentResponseBinding, MessageBuilder
            .withPayload(posts)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
            .build());
      } catch (Exception e) {
        log.error("Enrichment failed for request: {}", correlationId, e);
      }
    };
  }

  //todo: Ask for user full name
}
