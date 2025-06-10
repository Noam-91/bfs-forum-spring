package com.bfsforum.postservice.dao;

import com.bfsforum.postservice.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends MongoRepository<Post, String> {
  Page<Post> findByStatus(String status, Pageable pageable);

  @Query("{ 'title' : { '$regex' : ?0, '$options' : 'i' }, 'status' : 'PUBLISHED' }")
  Page<Post> findByTitleContainingAndPublished(String keyword, Pageable pageable);

  @Query("{ 'content' : { '$regex' : ?0, '$options' : 'i' }, 'status' : 'PUBLISHED' }")
  Page<Post> findByContentContainingAndPublished(String keyword, Pageable pageable);

  /** Search by full name */
  @Query("{ $and: [ { status: 'PUBLISHED' }, { $expr: { $regexMatch: { input: { $concat: ['$firstName', ' ', '$lastName'] }, regex: ?0, options: 'i' } } } ] }")
  Page<Post> findByFullNameRegex(String keywordRegex, Pageable pageable);

  @Query("{ 'status' : 'PUBLISHED' }")
  Page<Post> findAllPublished(Pageable pageable);

  @Query("{ 'userId' : ?0, 'status' : 'PUBLISHED' }")
  Page<Post> findByUserIdAndPublished(String userId, Pageable pageable);
}

