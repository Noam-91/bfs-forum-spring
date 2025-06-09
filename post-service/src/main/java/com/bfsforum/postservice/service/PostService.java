package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import com.bfsforum.postservice.exception.PostNotFoundException;
import com.bfsforum.postservice.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Service
@RequiredArgsConstructor
public class PostService {
	private final PostRepository postRepository;
//	private final KafkaMessageService kafkaMessageService;
	
	// retrieve all post
	public Page<Post> getAllPosts(int page, int size, String sortBy, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase("desc") ?
				Sort.by(sortBy).descending() :
				Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return postRepository.findAll(pageable);
	}
	
	// retrieve posts by status
	public Page<Post> getPostsByStatus(PostStatus status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return postRepository.findByStatus(status, pageable);
	}
	
	// retrieve published posts
	public Page<Post> getPublishedPosts(int page, int size, String sortBy, String sortDir) {
		Sort sort = sortDir.equalsIgnoreCase("desc") ?
				Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
		Pageable pageable = PageRequest.of(page, size, sort);
		return postRepository.findPublishedPosts(pageable);
	}
	
	// retrieve posts by userId
	public Page<Post> getPostsByUserId(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return postRepository.findByUserId(userId, pageable);
	}
	
	// retrieve posts by userId and Status
	public Page<Post> getPostsByUserIdAndStatus(Long userId, PostStatus status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return postRepository.findByUserIdAndStatus(userId, status, pageable);
	}
	
	// search posts by keywords
	public Page<Post> searchPosts(String keyword, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return postRepository.findByTitleOrContentContainingIgnoreCase(keyword, pageable);
	}
	
	// search posts by status and keywords
	public Page<Post> searchPostsByStatusAndKeyword(String keyword, PostStatus status, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
		return postRepository.findByKeywordAndStatus(keyword, status, pageable);
	}
	
	// retrieve users' drafts
	public Page<Post> getUserDrafts(Long userId, int page, int size) {
		Pageable pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
		return postRepository.findDraftPostsByUserId(userId, pageable);
	}
	
	// popular posts
	public Page<Post> getPopularPosts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return postRepository.findPopularPosts(pageable);
	}
	
	// recent posts
	public Page<Post> getLatestPosts(int page, int size) {
		Pageable pageable = PageRequest.of(page, size);
		return postRepository.findLatestPosts(pageable);
	}
	
	// create posts
	public Post createPost(Post post) {
		post.setCreatedAt(LocalDateTime.now());
		post.setUpdatedAt(LocalDateTime.now());
		return postRepository.save(post);
	}
	
	// create posts with attachments
	public Post createPostWithAttachments(String title, String content, Long userId,
	                                      List<String> imageUrls, List<String> fileUrls){
		
		if (title == null || title.equals("") || content == null || content.equals("")) {
			throw new IllegalArgumentException("Title or content cannot be empty");
		}
		
		Post post = new Post();
		post.setTitle(title);
		post.setContent(content);
		post.setUserId(userId);
		post.setStatus(PostStatus.UNPUBLISHED);
		post.setCreatedAt(LocalDateTime.now());
		post.setUpdatedAt(LocalDateTime.now());
		
		if (imageUrls != null && imageUrls.size() > 0) {
			post.setImages(imageUrls);
		}
		if (fileUrls != null && fileUrls.size() > 0) {
			post.setAttachments(fileUrls);
		}
		
		return postRepository.save(post);
	}
	
	// update posts
	public Post updatePost(String postId, Post updatedPost) {
		Optional<Post> existingPost = postRepository.findById(postId);
		if (existingPost.isPresent()) {
			Post post = existingPost.get();
			post.setTitle(updatedPost.getTitle());
			post.setContent(updatedPost.getContent());
			post.setStatus(updatedPost.getStatus());
			post.setIsArchived(updatedPost.getIsArchived());
			post.setUpdatedAt(LocalDateTime.now());
			return postRepository.save(post);
		}
		throw new PostNotFoundException(postId);
	}
	
	// upload attachments
	public List<String> uploadFiles(List<MultipartFile> files, String folderPath) {
		if (files == null || files.size() == 0) {
			return Collections.emptyList();
		}
		
		List<String> urls = new ArrayList<>();
		for (MultipartFile file : files) {
			String fileName = folderPath + UUID.randomUUID() + "-" + file.getOriginalFilename();
			try {
				urls.add("/uploads/" + fileName);
			} catch (Exception e) {
				throw new RuntimeException("Upload failed", e);
			}
		}
		
		return urls;
	}
	
	// update posts status
	public Post updatePostStatus(String postId, String status, Long userId, String role) {
		Post post = postRepository.findById(postId)
				.orElseThrow(() -> new PostNotFoundException(postId));
		
		boolean isOwner = post.getUserId().equals(userId);
		boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
		
		switch (status.toLowerCase()) {
			case "hide" -> {
				if (!isOwner && !isAdmin) {
					throw new UnauthorizedException("Not allowed to hide");
				}
				post.setStatus(PostStatus.HIDDEN);
			}
			case "unhide" -> {
				if (!isOwner && !isAdmin) {
					throw new UnauthorizedException("Not allowed to unhide");
				}
				post.setStatus(PostStatus.PUBLISHED);
			}
			case "archive" -> {
				if (!isOwner && !isAdmin) {
					throw new UnauthorizedException("Not allowed to archive");
				}
				post.setIsArchived(true);
			}
			case "unarchive" -> {
				if (!isOwner && !isAdmin) {
					throw new UnauthorizedException("Not allowed to unarchive");
				}
				post.setIsArchived(false);
			}
			case "ban" -> {
				if (!isAdmin) {
					throw new UnauthorizedException("Only admin can ban");
				}
				post.setStatus(PostStatus.BANNED);
			}
			case "unban" -> {
				if (!isAdmin) {
					throw new UnauthorizedException("Only admin can unban");
				}
				post.setStatus(PostStatus.PUBLISHED);
			}
			default -> throw new IllegalArgumentException("Unknown status " + status);
		}
		
		post.setUpdatedAt(LocalDateTime.now());
		return postRepository.save(post);
	}
	
	
	// retrieve posts by postId
	public Optional<Post> getPostById(String postId) {
		return postRepository.findById(postId);
	}
	
	// retrieve multiple posts by postIds
	public List<Post> getPostsByIds(List<String> postIds) {
		if (postIds == null || postIds.isEmpty()) {
			return Collections.emptyList();
		}
		
		return postRepository.findAllByIdIn(postIds);
	}
	
	// delete posts (soft delete) - not deleting from DB, just set the status
	public void deletePost(String postId) {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isPresent()) {
			Post existingPost = post.get();
			existingPost.setStatus(PostStatus.DELETED);
			existingPost.setUpdatedAt(LocalDateTime.now());
			postRepository.save(existingPost);
		} else {
			throw new PostNotFoundException(postId);
		}
	}
	
	// increasing the view count
	public void incrementViewCount(String postId) {
		Optional<Post> post = postRepository.findById(postId);
		if (post.isPresent()) {
			Post existingPost = post.get();
			existingPost.setViewCount(existingPost.getViewCount() + 1);
			postRepository.save(existingPost);
		}
	}
	
//	// add Kafka
//	@Override
//	public PostResponseDTO getPostById(String postId, Long viewUserId){
//		Post post = postRepository.findById(postId)
//				.orElseThrow(() -> new PostNotFoundException("Post not found with ID: " + postId));
//
//		// increase the view counts
//		post.setViewCount(post.getViewCount() + 1);
//		postRepository.save(post);
//
//		// send event of viewing posts
//		if (viewUserId != null) {
//			kafkaMessageService.sendPostViewedEvent(viewUserId, postId);
//		}
//
//		return convertoToResponseDTO(post);
//	}
//
//	@Override
//	public PostResponseDTO createPost(PostCreateDTO createDTO){
//		Post post = convertToEntity(createDTO);
//		post.setCreatedAt(LocalDateTime.now());
//		post.setUpdatedAt(LocalDateTime.now());
//
//		Post savedPost = postRepository.save(post);
//
//		// send event of creating posts
//		kafkaMessageService.sendPostCreatedEvent(
//				savedPost.getId(),
//				savedPost.getUserId(),
//				savedPost.getTitle()
//		);
//
//		return convertToResponseDTO(savedPost);
//	}
//
}
