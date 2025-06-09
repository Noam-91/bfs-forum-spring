package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.config.KafkaConsumerConfig;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import com.bfsforum.postservice.dto.PostCreateDTO;
import com.bfsforum.postservice.dto.PostUpdateDTO;
import com.bfsforum.postservice.exception.PostNotFoundException;
import com.bfsforum.postservice.exception.UnauthorizedException;
import com.bfsforum.postservice.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@RestController
@RequestMapping("/posts")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class PostController {
	
	private final PostService postService;
	private final KafkaConsumerConfig kafkaConsumerConfig;
	
	@GetMapping
	public ResponseEntity<Page<Post>> getAllPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String status) {
		
		Page<Post> posts;
		if (status != null && !status.isEmpty()) {
			PostStatus postStatus = PostStatus.valueOf(status.toUpperCase());
			posts = postService.getPostsByStatus(postStatus, page, size);
		} else {
			posts = postService.getAllPosts(page, size, sortBy, sortDir);
		}
		
		return ResponseEntity.ok(posts);
	}
	
	@GetMapping("/published")
	public ResponseEntity<Page<Post>> getPublishedPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir) {
		
		Page<Post> posts = postService.getPublishedPosts(page, size, sortBy, sortDir);
		
		return ResponseEntity.ok(posts);
	}
	
	@GetMapping("/{id}")
	public ResponseEntity<Post> getPostById(
			@PathVariable String id,
			@RequestHeader(value = "User-Id", required = false) Long userId) {
		
		Post post = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
		
		// increase view counts
		postService.incrementViewCount(id);
		
		if (userId != null) {
			try {
				kafkaConsumerConfig.sendPostViewNotification(userId, id);
			} catch (Exception e) {
				log.warn("Failed to send post view notification: userId={}, postId={}", userId, id, e);
			}
		}
		
		return ResponseEntity.ok(post);
	}
	
	@PostMapping
	public ResponseEntity<Post> createPost(
			@Valid @RequestBody PostCreateDTO createDTO,
			@RequestHeader("User-Id") Long userId) {
		
		Post post = new Post();
		post.setTitle(createDTO.getTitle());
		post.setContent(createDTO.getContent());
		post.setUserId(userId);
		post.setStatus(PostStatus.UNPUBLISHED);
		
		Post createdPost = postService.createPost(post);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Post> updatePost(
			@PathVariable String id,
			@Valid @RequestBody PostUpdateDTO updateDTO,
			@RequestHeader("User-Id") Long userId) {
		
		Post existingPost = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
		
		if (!existingPost.getUserId().equals(userId)) {
			throw new UnauthorizedException("You can only update your own posts");
		}
		Post updatePost = new Post();
		updatePost.setTitle(updateDTO.getTitle());
		updatePost.setContent(updateDTO.getContent());
		updatePost.setUserId(userId);
		
		Post updatedPost = postService.updatePost(id, updatePost);
		return ResponseEntity.ok(updatedPost);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(
			@PathVariable String id,
			@RequestHeader("User-Id") Long userId,
			@RequestHeader(value = "Role", defaultValue = "USER") String role) {
		
		Post post = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
		
		// verification
		boolean isOwner = post.getUserId().equals(userId);
		boolean isAdmin = role.equalsIgnoreCase("ADMIN");
		
		if (!isOwner && !isAdmin) {
			throw new UnauthorizedException("You can only delete your own posts");
		}
		
		postService.deletePost(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/search")
	public ResponseEntity<Page<Post>> searchPosts(
			@RequestParam String keyword,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String status) {
		
		Page<Post> posts;
		if (status != null && !status.isEmpty()) {
			PostStatus postStatus = PostStatus.valueOf(status.toUpperCase());
			posts = postService.searchPostsByStatusAndKeyword(keyword, postStatus, page, size);
		} else {
			posts = postService.searchPosts(keyword, page, size);
		}
		
		return ResponseEntity.ok(posts);
	}
	
	@GetMapping("/user/{userId}")
	public ResponseEntity<Page<Post>> getPostsByUserId(
			@PathVariable Long userId,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String status) {
		
		Page<Post> posts;
		if (status != null && !status.isEmpty()) {
			PostStatus postStatus = PostStatus.valueOf(status.toUpperCase());
			posts = postService.getPostsByUserIdAndStatus(userId, postStatus, page, size);
		} else {
			posts = postService.getPostsByUserId(userId, page, size);
		}
		
		return ResponseEntity.ok(posts);
	}
	
	@GetMapping("/my-drafts")
	public ResponseEntity<Page<Post>> getMyDrafts(
			@RequestParam Long userId, // 实际项目中从JWT token获取
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		Page<Post> drafts = postService.getUserDrafts(userId, page, size);
		return ResponseEntity.ok(drafts);
	}
	
	@GetMapping("/popular")
	public ResponseEntity<Page<Post>> getPopularPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		Page<Post> posts = postService.getPopularPosts(page, size);
		return ResponseEntity.ok(posts);
	}
	
	@GetMapping("/latest")
	public ResponseEntity<Page<Post>> getLatestPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size) {
		
		Page<Post> posts = postService.getLatestPosts(page, size);
		return ResponseEntity.ok(posts);
	}
	
	@PutMapping("/{id}/status")
	public ResponseEntity<Post> updatePostStatus(
			@PathVariable String id,
			@RequestParam String status,
			@RequestHeader("User-Id") Long userId,
			@RequestHeader(value = "Role", defaultValue = "USER") String role){
	
		Post updatedPost = postService.updatePostStatus(id, status, userId, role);
		return ResponseEntity.ok(updatedPost);
	}
	
//	// admin
//	@PutMapping("/{id}/ban")
//	public ResponseEntity<Post> banPost(@PathVariable String id) {
//
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//
//		post.setStatus(PostStatus.BANNED);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
//
//	@PutMapping("/{id}/unban")
//	public ResponseEntity<Post> unbanPost(@PathVariable String id) {
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//		post.setStatus(PostStatus.PUBLISHED);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
//
//	@PutMapping("/{id}/archive")
//	public ResponseEntity<Post> archivePost(@PathVariable String id,
//	                                        @RequestHeader("User-Id") Long userId,
//	                                        @RequestHeader(value = "Role", defaultValue = "USER") String role) {
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//		post.setIsArchived(true);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
//
//	@PutMapping("/{id}/unarchive")
//	public ResponseEntity<Post> unarchivePost(@PathVariable String id,
//	                                          @RequestHeader("User-Id") Long userId,
//	                                          @RequestHeader(value = "Role", defaultValue = "USER") String role) {
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//
//		post.setIsArchived(false);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
//
//	@PutMapping("/{id}/hide")
//	public ResponseEntity<Post> hidePost(@PathVariable String id,
//	                                     @RequestHeader("User-Id") Long userId,
//	                                     @RequestHeader(value = "Role", defaultValue = "USER") String role) {
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//		post.setStatus(PostStatus.HIDDEN);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
//
//	@PutMapping("/{id}/unhide")
//	public ResponseEntity<Post> unhidePost(@PathVariable String id,
//	                                       @RequestHeader("User-Id") Long userId,
//	                                       @RequestHeader(value = "Role", defaultValue = "USER") String role) {
//		Post post = postService.getPostById(id)
//				.orElseThrow(() -> new PostNotFoundException(id));
//		post.setStatus(PostStatus.PUBLISHED);
//		Post updatedPost = postService.updatePost(id, post);
//		return ResponseEntity.ok(updatedPost);
//	}
}
