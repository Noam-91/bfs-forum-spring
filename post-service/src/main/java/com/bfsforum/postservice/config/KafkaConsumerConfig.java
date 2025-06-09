package com.bfsforum.postservice.config;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.kafka.PostDTO;
import com.bfsforum.postservice.dto.kafka.PostViewNotification;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentRequest;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentResponse;
import com.bfsforum.postservice.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class KafkaConsumerConfig {
  private final PostService postService;
  private final StreamBridge streamBridge;

  @Value("${app.kafka.topics.post-view-notification}")
  private String postViewNotificationBinding;
  
  @Value("${app.kafka.topics.posts-enrichment-response}")
  private String postEnrichmentResponseBinding;

//  public KafkaConsumerConfig(PostService postService, StreamBridge streamBridge) {
//    this.postService = postService;
//    this.streamBridge = streamBridge;
//  }

  // Consumer: handle enrichment request
  @Bean
  public Consumer<PostsEnrichmentRequest> postsEnrichmentConsumer() {
    return request -> {
      try {
        log.info("Processing enrichment request: {}", request.getRequestId());
        
        List<Post> posts = postService.getPostsByIds(request.getPostIds());
        
        List<PostDTO> postDTOS = posts.stream()
                .map(this::convertToPostDTO)
                .collect(Collectors.toList());
        
        sendEnrichmentResponse(request.getRequestId(), postDTOS, null);
        
      } catch (Exception e){
        log.error("Enrichment failed for request: {}", request.getRequestId(), e);
        sendEnrichmentResponse(request.getRequestId(), Collections.emptyList(), e.getMessage());
      }
    };
  }
  
  // Producer 1: send postViewNotification
  public void sendPostViewNotification(Long userId, String postId) {
    try {
      PostViewNotification notification = PostViewNotification.builder()
              .userId(userId)
              .postId(postId)
              .viewedAt(LocalDateTime.now())
              .build();
      
      streamBridge.send(postViewNotificationBinding, notification);
      log.info("Successfully sent post view notification: userId={}, postId={}", userId, postId);
      
    } catch (Exception e){
      log.error("Notification failed: userId={}, error={}", userId, e);
    }
  }
  
  // Producer 2: send enrichmentResponse
  private void sendEnrichmentResponse(UUID requestId, List<PostDTO> posts, String error) {
    try{
      PostsEnrichmentResponse response = PostsEnrichmentResponse.builder()
              .requestId(requestId)
              .posts(posts)
              .timestamp(LocalDateTime.now())
              .error(error)
              .build();
      
      streamBridge.send(postEnrichmentResponseBinding, response);
      log.info("Enrichment response sent: requestId={}, success={}", requestId, error == null);
      
    } catch (Exception e){
      log.error("Enrichment response failed: requestId={}, error={}", requestId, error);
    }
  }
  
  private PostDTO convertToPostDTO(Post post) {
    return new PostDTO(
            post.getId(),
            post.getUserId(),
            post.getTitle(),
            post.getContent(),
            post.getStatus(),
            post.getIsArchived(),
            post.getCreatedAt(),
            post.getUpdatedAt(),
            post.getViewCount(),
            post.getReplyCount()
    );
  }
}
