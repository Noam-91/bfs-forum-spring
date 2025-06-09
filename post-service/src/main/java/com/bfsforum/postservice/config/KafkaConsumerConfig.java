package com.bfsforum.postservice.config;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.exception.PostNotFoundException;
import com.bfsforum.postservice.service.PostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import java.util.function.Consumer;

@Configuration
@Slf4j
public class KafkaConsumerConfig {
  private final PostService postService;
  private final StreamBridge streamBridge;

  @Value("${app.kafka.topics.post-view-notification}")
  private String bindingName;

  public KafkaConsumerConfig(PostService postService, StreamBridge streamBridge) {
    this.postService = postService;
    this.streamBridge = streamBridge;
  }

  @Bean
  public Consumer<Message<String>> historyEventConsumer() {
    return message -> {
      String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
      String postId = message.getPayload();
      Post post = postService.getPostById(postId)
              .orElseThrow(() -> new PostNotFoundException(postId));
      Message<Post> replyMessage = MessageBuilder.withPayload(post)
          .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
          .build();
      streamBridge.send(bindingName, replyMessage);
    };
  }
}
