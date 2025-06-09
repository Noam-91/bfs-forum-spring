package com.bfsforum.postservice.dao;

import com.bfsforum.postservice.domain.Reply;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@Repository
public interface ReplyRepository extends MongoRepository<Reply, String> {
	List<Reply> findByPostIdOrderByCreatedAtAsc(String postId);
	
	long countByPostId(String postId);
}
