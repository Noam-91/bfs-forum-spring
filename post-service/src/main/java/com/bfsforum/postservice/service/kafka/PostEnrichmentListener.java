package com.bfsforum.postservice.service.kafka;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.kafka.PostDTO;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentRequest;
import com.bfsforum.postservice.dto.kafka.PostsEnrichmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

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
	@KafkaListener(topics = "posts-enrichment-request")
	@SendTo("posts-enrichment-response")
	public PostsEnrichmentResponse handlePostsEnrichmentRequest(PostsEnrichmentRequest request) {
		log.info("Received posts enrichment request: requestId={}, postId={}", request.getRequestId(), request.getPostIds());
		
		try {
			// search posts by postId
			List<Post> posts = postRepository.findAllByPostIdIn(request.getPostIds());
			
			// convert into DTO
			List<PostDTO> postDTOs = posts.stream()
					.map(this::converToPostDTO)
					.collect(Collectors.toList());
			
			PostsEnrichmentResponse response = new PostsEnrichmentResponse(
					request.getRequestId(),
					postDTOs
			);
			
			log.info("Sending posts enrichment response: requestId={}, postCount={}", request.getRequestId(), postDTOs.size());
			
			return response;
		} catch (Exception e){
			log.error("Error processing posts enrichment request: requestId={}", request.getRequestId(), e);
			
			return new PostsEnrichmentResponse(request.getRequestId(), List.of());
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
