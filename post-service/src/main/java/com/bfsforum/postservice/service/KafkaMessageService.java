package com.bfsforum.postservice.service;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.kafka.*;
import com.bfsforum.postservice.exception.ServiceUnavailableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {
	
	// ==== old version ====
	@Value("${app.kafka.topics.post-viewed:post-viewed}")
	private String postViewedTopic;
	
	@Value("${app.kafka.topics.post-created:post-created}")
	private String postCreatedTopic;
	
	// ==== end old version ====
	
	@Value("${app.kafka.topics.post-view-notification:post-view-notification}")
	private String postViewNotificationTopic;
	
	@Value("${app.kafka.topics.posts-enrichment-response:posts-enrichment-response}")
	private String postsEnrichmentResponseTopic;
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public void sendPostViewNotification(Long userId, String postId) {
		try {
			PostViewNotification notification = PostViewNotification.builder()
					.userId(userId)
					.postId(postId)
					.viewedAt(LocalDateTime.now())
					.build();
			
			kafkaTemplate.send(postViewNotificationTopic, postId, notification)
					.whenComplete((result, ex) -> {
						if (ex != null) {
							log.error("Post view notification failed: userId={}, postId={}", userId, postId, ex);
						} else {
							log.debug("Post view notification sent successfully: userId={}, postId={}", userId, postId);
						}
					});
		} catch (Exception e) {
			log.error("Error sending post view notification: userId={}, postId={}", userId, postId, e);
			throw new ServiceUnavailableException("Error sending post view notification: " + e.getMessage());
		}
	}
	
	public void sendPostsEnrichmentResponse(UUID requestId, List<PostDTO> posts) {
		try{
			PostsEnrichmentResponse response = PostsEnrichmentResponse.builder()
					.requestId(requestId)
					.posts(posts)
					.timestamp(LocalDateTime.now())
					.build();
			
			kafkaTemplate.send(postsEnrichmentResponseTopic, requestId.toString(), response)
					.whenComplete((result, ex) -> {
						if (ex != null) {
							log.error("Posts enrichment response failed: correlationId={}", requestId, ex);
						} else {
							log.debug("Posts enrichment response sent successfully: correlationId={}", requestId);
						}
					});
		} catch (Exception e){
			log.error("Error sending posts enrichment response: correlationId={}", requestId, e);
			throw new ServiceUnavailableException("Error sending posts enrichment response: " + e.getMessage());
		}
	}
	
	public PostsEnrichmentResponse createPostsEnrichmentResponse(UUID requestId, List<PostDTO> posts) {
		return PostsEnrichmentResponse.builder()
				.requestId(requestId)
				.posts(posts)
				.timestamp(LocalDateTime.now())
				.build();
	}
	
//	// event of sending viewing posts
//	public void sendPostViewedEvent(Long userId, String postId){
//		try {
//			PostViewedEvent event = new PostViewedEvent(userId, postId, LocalDateTime.now());
//			kafkaTemplate.send(postViewedTopic, postId, event)
//					.whenComplete((result, ex) -> {
//						if(ex != null) {
//							log.error("Post viewed event failed: userId={}, postId={}", userId, postId, ex);
//						} else {
//							log.debug("Post viewed event sent successfully: userId={}, postId={}", userId, postId, ex);
//						}
//					});
//		} catch (Exception e){
//			log.error("Error sending post viewed event: userId={}, postId={}", userId, postId, e );
//			throw new ServiceUnavailableException("Error sending post viewed event: userId=" + userId, e);
//		}
//	}
//
//	public void sendPostCreatedEvent(Long userId, String postId, String title){
//		// event of sending creating posts
//		try {
//			PostCreatedEvent event = new PostCreatedEvent(postId, userId, title, LocalDateTime.now());
//
//			kafkaTemplate.send(postCreatedTopic, postId, event)
//					.whenComplete((result, ex) -> {
//						if(ex != null) {
//							log.error("Post created event failed: userId={}, postId={}", userId, postId, ex);
//						} else {
//							log.debug("Post created event sent successfully: userId={}, postId={}", userId, postId, ex);
//						}
//					});
//		} catch (Exception e){
//			log.error("Error sending post created event: userId={}, postId={}", userId, postId, e );
//			throw new ServiceUnavailableException("Error sending post created event: userId=" + userId, e);
//		}
//	}
}
