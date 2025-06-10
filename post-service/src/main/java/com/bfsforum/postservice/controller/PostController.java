package com.bfsforum.postservice.controller;

import com.bfsforum.postservice.config.KafkaConsumerConfig;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.exception.ErrorResponse;
import com.bfsforum.postservice.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Post", description = "Post API")
public class PostController {
	private final PostService postService;
	public PostController(PostService postService) {
		this.postService = postService;
	}

	@GetMapping
	@Operation(summary = "Get posts by query parameters", description = "status, search, userId")
	@ApiResponse(responseCode = "200", description = "Posts retrieved",
			content = @Content(mediaType = "application/json",
					array = @ArraySchema(schema = @Schema(implementation = Post.class))))
	@ApiResponse(responseCode = "400", description = "Bad Request",
		content = @Content(mediaType = "application/json",
			schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Page<Post>> getQueriedPosts(
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String sortDir,
			@RequestParam(required = false) String status,
			@RequestParam(required = false) String keyword,
			@RequestParam(required = false) String searchIn,
			@RequestParam(required = false) String userId,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String viewerId,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String viewerRole) {

			Page<Post> posts = postService.getQueriedPosts(page, size, sortBy, sortDir,
					status, keyword, searchIn, userId, viewerId, viewerRole);
		return ResponseEntity.ok(posts);
	}

	@PostMapping
	@Operation(summary = "Create post", description = "Create a new post")
	@ApiResponse(responseCode = "201", description = "Post created",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> createPost(
			@Valid @RequestBody Post post,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String creatorRole,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String creatorId) {

		Post createdPost = postService.createPost(post, creatorId, creatorRole);
		return ResponseEntity.status(HttpStatus.CREATED).body(createdPost);
	}

	@GetMapping("/{postId}")
	@Operation(summary = "Get post by id", description = "Get a post by id")
	@ApiResponse(responseCode = "200", description = "Post retrieved",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Not Found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> getPostById(
			@PathVariable String postId,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userId) {

		Post post = postService.getPostById(postId, userId);
		return ResponseEntity.ok(post);
	}

	@PutMapping("/{postId}")
	@Operation(summary = "Update post", description = "Update a post")
	@ApiResponse(responseCode = "200", description = "Post updated",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Not Found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> updatePost(
			@PathVariable String postId,
			@Valid @RequestBody Post post,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userId,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String userRole) {

		Post updatedPost = postService.updatePost(postId, post, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	@PostMapping("/{postId}")
	@Operation(summary = "Reply post", description = "Reply a post")
	@ApiResponse(responseCode = "200", description = "Post replied",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Not Found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> replyPost(
			@PathVariable String postId,
			@RequestParam(required = false) String replyId,
			@RequestBody String comment,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userId,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String userRole) {
		Post updatedPost = postService.replyPost(postId, replyId, comment, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	/** Transfer post status */
	@PatchMapping("/{postId}")
	@Operation(summary = "Transfer post status", description = "Operations: BAN, UNBAN, HIDE, SHOW, DELETE, RECOVER, ARCHIVE, UNARCHIVE")
	@ApiResponse(responseCode = "200", description = "Post status transferred",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Not Found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> transferPostStatus(
			@RequestParam String operation,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String userId,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String userRole,
			@PathVariable String postId) {
		Post updatedPost = postService.transferPostStatus(postId, operation, userId, userRole);
		return ResponseEntity.ok(updatedPost);
	}

	/** Transfer reply visibility */
	@PatchMapping("/{postId}/reply/{replyId}")
	@Operation(summary = "Transfer reply visibility", description = "Transfer reply visibility")
	@ApiResponse(responseCode = "200", description = "Reply visibility transferred",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = Post.class)))
	@ApiResponse(responseCode = "400", description = "Bad Request",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "403", description = "Forbidden",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	@ApiResponse(responseCode = "404", description = "Not Found",
			content = @Content(mediaType = "application/json",
					schema = @Schema(implementation = ErrorResponse.class)))
	public ResponseEntity<Post> toggleReplyActive(
			@PathVariable String replyId,
			@RequestParam(required = false) String subReplyId,
			@RequestHeader(value = "X-User-Id") @Parameter(hidden = true) String updaterId,
			@RequestHeader(value = "X-User-Role") @Parameter(hidden = true) String updaterRole,
			@PathVariable String postId) {
		Post updatedPost = postService.toggleReplyActive(postId, replyId, subReplyId, updaterId, updaterRole);
		return ResponseEntity.ok(updatedPost);
	}

}
