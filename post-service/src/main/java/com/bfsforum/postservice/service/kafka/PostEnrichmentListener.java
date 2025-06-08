package com.bfsforum.postservice.service.kafka;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.kafka.PostDTO;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentRequest;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class PostEnrichmentListener {
	
	private final PostRepository postRepository;
	
	/**
	 * monitor the post request
	 * return  info
	 * */
	@KafkaListener(
			topics = "${app.kafka.topics.posts-enrichment-request:posts-enrichment-request}",
			groupId = "${spring.kafka.consumer.group-id:post-service-group}",
			containerFactory = "enrichmentRequestListenerFactory"
	)
	@SendTo("${app.kafka.topics.posts-enrichment-response:posts-enrichment-response}")
	public PostsEnrichmentResponse handlePostsEnrichmentRequest(
			PostsEnrichmentRequest request,
			Acknowledgment ack,
			@Header("kafka_receivedTopic") String topic,
			@Header("kafka_receivedPartition") int partition,
			@Header("kafka_offset") long offset) {
		log.info("Received posts enrichment request: requestId={}, postIds={}, topic={}, partition={}, offset={}",
				request.getRequestId(), request.getPostIds(), topic, partition, offset);
		
		try {
			// search posts by postId
			List<Post> posts = postRepository.findAllByPostIdIn(request.getPostIds());
			
			// convert into DTO
			List<PostDTO> postDTOs = posts.stream()
					.map(this::converToPostDTO)
					.collect(Collectors.toList());
			
			PostsEnrichmentResponse response = PostsEnrichmentResponse.builder()
					.requestId(request.getRequestId())
					.posts(postDTOs)
					.timestamp(LocalDateTime.now())
					.build();
			
			// confirm message
			ack.acknowledge();
			
			log.info("Sending posts enrichment response: requestId={}, postCount={}", request.getRequestId(), postDTOs.size());
			
			return response;
		} catch (Exception e){
			log.error("Error processing posts enrichment request: requestId={}", request.getRequestId(), e);
			
			PostsEnrichmentResponse errorResponse = PostsEnrichmentResponse.builder()
					.requestId(request.getRequestId())
					.posts(Collections.emptyList())
					.timestamp(LocalDateTime.now())
					.error("Failed to process request: " + e.getMessage())
					.build();
			
			ack.acknowledge();
			return errorResponse;
		}
	}
	
	/**
	 * convert post into PostDTO
	 * */
	private PostDTO converToPostDTO(Post post) {
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
