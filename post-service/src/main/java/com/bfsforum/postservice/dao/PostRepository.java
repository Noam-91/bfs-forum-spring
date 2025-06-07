package com.bfsforum.postservice.dao;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-06
 */

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
	
	Page<Post> findAll(Pageable pageable);
	
	Page<Post> findByStatus(PostStatus status, Pageable pageable);
	
	Page<Post> findByUserId(Long userId, Pageable pageable);
	
	Page<Post> findByUserIdAndStatus(Long userId, PostStatus status, Pageable pageable);
	
	List<Post> findAllByPostIdIn(List<String> postId);
	
	// search post by title or content
	@Query("{'$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'content': {'$regex': ?0, '$options': 'i'}}]}")
	Page<Post> findByTitleOrContentContainingIgnoreCase(String keyword, Pageable pageable);
	
	// search by keyword and status
	@Query("{'status': ?1, '$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'content': {'$regex': ?0, '$options': 'i'}}]}")
	Page<Post> findByKeywordAndStatus(String keyword, PostStatus status, Pageable pageable);
	
	// retrieve pulished
	@Query("{'status': 'PUBLISHED'}")
	Page<Post> findPublishedPosts(Pageable pageable);
	
	// search by creation time
	Page<Post> findByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
	
	// search published by userId
	@Query("{'userId': ?0, 'status': 'PUBLISHED'}")
	Page<Post> findPublishedPostsByUserId(Long userId, Pageable pageable);
	
	// search draft by userId
	@Query("{'userId': ?0, 'status': 'UNPUBLISHED'}")
	Page<Post> findDraftPostsByUserId(Long userId, Pageable pageable);
	
	// retrieve the popular posts
	@Query(value = "{}", sort = "{'replyCount': -1}")
	Page<Post> findPopularPosts(Pageable pageable);
	
	// retrieve recent posts
	@Query(value = "{}", sort = "{'createdAt': -1}")
	Page<Post> findLatestPosts(Pageable pageable);
	
	// search with no pagination
	List<Post> findByStatusOrderByCreatedAtDesc(PostStatus status);
	List<Post> findByUserIdOrderByCreatedAtDesc(Long userId);
}
