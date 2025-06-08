package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dto.kafka.PostCreatedEvent;
import com.bfsforum.postservice.dto.kafka.PostViewedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaMessageService {
	
	@Value("${app.kafka.topics.post-viewed:post-viewed}")
	private String postViewedTopic;
	
	@Value("${app.kafka.topics.post-created:post-created}")
	private String postCreatedTopic;
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	// event of sending viewing posts
	public void sendPostViewedEvent(Long userId, String postId){
		try {
			PostViewedEvent event = new PostViewedEvent(userId, postId, LocalDateTime.now());
			kafkaTemplate.send(postViewedTopic, postId, event)
					.whenComplete((result, ex) -> {
						if(ex != null) {
							log.error("Post viewed event failed: userId={}, postId={}", userId, postId, ex);
						} else {
							log.debug("Post viewed event sent successfully: userId={}, postId={}", userId, postId, ex);
						}
					});
		} catch (Exception e){
			log.error("Error sending post viewed event: userId={}, postId={}", userId, postId, e );
		}
	}
	
	public void sendPostCreatedEvent(Long userId, String postId, String title){
		// event of sending creating posts
		try {
			PostCreatedEvent event = new PostCreatedEvent(postId, userId, title, LocalDateTime.now());
			
			kafkaTemplate.send(postCreatedTopic, postId, event)
					.whenComplete((result, ex) -> {
						if(ex != null) {
							log.error("Post created event failed: userId={}, postId={}", userId, postId, ex);
						} else {
							log.debug("Post created event sent successfully: userId={}, postId={}", userId, postId, ex);
						}
					});
		} catch (Exception e){
			log.error("Error sending post created event: userId={}, postId={}", userId, postId, e );
		}
	}
}
