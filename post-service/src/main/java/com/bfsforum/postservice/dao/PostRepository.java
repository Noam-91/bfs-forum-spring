package com.bfsforum.postservice.dao;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.StatusCountProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
  Page<Post> findByStatus(String status, Pageable pageable);

  @Query("{ 'title' : { '$regex' : ?0, '$options' : 'i' }, 'status' : 'PUBLISHED' }")
  Page<Post> findByTitleContainingAndPublished(String keyword, Pageable pageable);

  @Query("{ 'status' : 'PUBLISHED' }")
  Page<Post> findAllPublished(Pageable pageable);

  @Query("{ 'userId' : ?0, 'status' : 'PUBLISHED' }")
  Page<Post> findByUserIdAndPublished(String userId, Pageable pageable);

  @Aggregation(pipeline = {
      "{ '$group': { '_id' : '$status', 'count' : { '$sum' : 1 } } }"
  })
  List<StatusCountProjection> countPostsByStatus();
}

