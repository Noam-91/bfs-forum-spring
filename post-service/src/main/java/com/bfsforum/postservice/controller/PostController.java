package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.config.KafkaConsumerConfig;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@RestController
@RequestMapping("/posts")
@Slf4j
public class PostController {
	private final PostService postService;
	public PostController(PostService postService) {
		this.postService = postService;
	}
	
	@GetMapping
	public ResponseEntity<Page<Post>> getQueriedPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String searchIn,
			@RequestParam(required = false) String userId,
			@RequestHeader(value = "X-User-Id") String viewerId,
			@RequestHeader(value = "X-User-Role") String viewerRole) {

			Page<Post> posts = postService.getQueriedPosts(page, size, sortBy, sortDir,
					status, keyword, searchIn, userId, viewerId, viewerRole);
		return ResponseEntity.ok(posts);
	}

	@PostMapping
	public ResponseEntity<Post> createPost(
			@Valid @RequestBody Post post,
			@RequestHeader(value = "X-User-Role") String creatorRole,
			@RequestHeader(value = "X-User-Id") String creatorId) {

		Post createdPost = postService.createPost(post, creatorId, creatorRole);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}

	@GetMapping("/{postId}")
	public ResponseEntity<Post> getPostById(
			@PathVariable String postId,
			@RequestHeader(value = "X-User-Id") String userId) {

		Post post = postService.getPostById(postId, userId);
		return ResponseEntity.ok(post);
	}

	@PutMapping("/{postId}")
	public ResponseEntity<Post> updatePost(
			@PathVariable String postId,
			@Valid @RequestBody Post post,
			@RequestHeader(value = "X-User-Id") String userId,
			@RequestHeader(value = "X-User-Role") String userRole) {

		Post updatedPost = postService.updatePost(postId, post, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	@PostMapping("/{postId}")
	public ResponseEntity<Post> replyPost(
			@PathVariable String postId,
			@RequestParam(required = false) String replyId,
			@RequestBody String comment,
			@RequestHeader(value = "X-User-Id") String userId,
			@RequestHeader(value = "X-User-Role") String userRole) {
		Post updatedPost = postService.replyPost(postId, replyId, comment, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	/** Transfer post status */
	@PatchMapping("/{postId}")
	public ResponseEntity<Post> transferPostStatus(
			@RequestParam String operation,
			@RequestHeader(value = "X-User-Id") String userId,
			@RequestHeader(value = "X-User-Role") String userRole,
			@PathVariable String postId) {
		Post updatedPost = postService.transferPostStatus(postId, operation, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	/** Transfer reply visibility */
	@PatchMapping("/{postId}/reply/{replyId}")
	public ResponseEntity<Post> toggleReplyActive(
			@PathVariable String replyId,
			@RequestParam(required = false) String subReplyId,
			@RequestHeader(value = "X-User-Id") String updaterId,
			@RequestHeader(value = "X-User-Role") String updaterRole,
			@PathVariable String postId) {
		Post updatedPost = postService.toggleReplyActive(postId, replyId, subReplyId, updaterId, updaterRole);
		return ResponseEntity.ok(updatedPost);
	}

}
