package com.bfsforum.postservice.dao;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-06
 */
public interface PostRepository extends MongoRepository<Post, String> {
	
	Page<Post> findByStatus(PostStatus status, Pageable pageable);
	
	Page<Post> findByUserId(Long userId, Pageable pageable);
	
	Page<Post> findByUserIdAndStatus(Long userId, PostStatus status, Pageable pageable);
	
	@Query("{'$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'content': {'$regex': ?0, '$options': 'i'}}]}")
	Page<Post> findByTitleOrContentContainingIgnoreCase(String keyword, Pageable pageable);
	
	@Query("{'status': ?1, '$or': [{'title': {'$regex': ?0, '$options': 'i'}}, {'content': {'$regex': ?0, '$options': 'i'}}]}")
	Page<Post> findByKeywordAndStatus(String keyword, PostStatus status, Pageable pageable);
	
	@Query("{'status': 'PUBLISHED'}")
	Page<Post> findPublishedPosts(Pageable pageable);
}
