package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import com.bfsforum.postservice.exception.PostNotFoundException;
import com.bfsforum.postservice.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
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
public class PostController {
	@Autowired
	private PostService postService;
	
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
	public ResponseEntity<Post> getPostById(@PathVariable String id) {
		Post post = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));

		// increase view counts
		postService.incrementViewCount(id);
		return ResponseEntity.ok(post);
	}
	
	@PostMapping
	public ResponseEntity<Post> createPost(@Valid @RequestBody Post post) {
		Post createdPost = postService.createPost(post);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<Post> updatePost(@PathVariable String id, @Valid @RequestBody Post post) {
			Post updatedPost = postService.updatePost(id, post);
			return ResponseEntity.ok(updatedPost);
	}
	
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deletePost(@PathVariable String id) {
			postService.deletePost(id);
			return ResponseEntity.noContent().build();
	}
	
	@GetMapping("/search")
	public ResponseEntity<Page<Post>> searchPosts(
			@RequestParam String query,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(required = false) String status) {
		
		Page<Post> posts;
		if (status != null && !status.isEmpty()) {
			PostStatus postStatus = PostStatus.valueOf(status.toUpperCase());
			posts = postService.searchPostsByStatusAndKeyword(query, postStatus, page, size);
		} else {
			posts = postService.searchPosts(query, page, size);
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
	
	// admin
	@PutMapping("/{id}/ban")
	public ResponseEntity<Post> banPost(@PathVariable String id) {

		Post post = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
		
		post.setStatus(PostStatus.BANNED);
		Post updatedPost = postService.updatePost(id, post);
		return ResponseEntity.ok(updatedPost);
	}
	
	@PutMapping("/{id}/unban")
	public ResponseEntity<Post> unbanPost(@PathVariable String id) {
		Post post = postService.getPostById(id)
				.orElseThrow(() -> new PostNotFoundException(id));
		post.setStatus(PostStatus.PUBLISHED);
		Post updatedPost = postService.updatePost(id, post);
		return ResponseEntity.ok(updatedPost);
	}
}
