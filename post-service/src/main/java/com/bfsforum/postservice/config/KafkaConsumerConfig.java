package com.bfsforum.postservice.config;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.UserInfo;
import com.bfsforum.postservice.service.PostService;
import com.bfsforum.postservice.service.RequestReplyManager;
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

  @Value("${app.kafka.topics.posts-enrichment-response}")
  private String postEnrichmentResponseBinding;

  @Bean
  public Consumer<Message<List<String>>> postsEnrichmentConsumer(
      PostService postService,
      StreamBridge streamBridge
  ) {
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

  @Bean
  public Consumer<Message<List<UserInfo>>> userInfoEventConsumer(
      RequestReplyManager<List<UserInfo>> requestReplyManager) {
    return message -> {
      log.info("Received userInfo reply");
      String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
      try {
        List<UserInfo> userInfos = message.getPayload();
        requestReplyManager.completeFuture(correlationId, userInfos);
      } catch (Exception e) {
        log.error("Enrichment failed for request: {}", correlationId, e);
      }
    };
  }

}
