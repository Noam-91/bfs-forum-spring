package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.*;
import com.bfsforum.postservice.exception.NotAuthorizedException;
import com.bfsforum.postservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Transactional
@Service
@Slf4j
public class PostService {
  private final PostRepository postRepository;
  private final StreamBridge  streamBridge;

  @Value("${app.kafka.topics.post-view-notification}")
  private String postViewBindingName;

  public PostService(PostRepository postRepository, StreamBridge streamBridge) {
    this.postRepository = postRepository;
    this.streamBridge = streamBridge;
  }

  /**
   * Get post by id.
   * increment view count.
   * send view event to kafka.
   * @param postId post id
   * @param userId user id
   * @return post
   * */
  public Post getPostById(String postId, String userId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    if (!post.getStatus().equals(PostStatus.PUBLISHED.name()) && !post.getUserId().equals(userId)) {
      throw new NotAuthorizedException("Only verified users can view unpublished posts");
    }
    // viewCount increment
    post.setViewCount(post.getViewCount() + 1);
    Post savedPost = postRepository.save(post);

    // streamBridge send view event
    String correlationId = UUID.randomUUID().toString();
    Message<Post> message = MessageBuilder.withPayload(savedPost)
        .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
        .build();
    streamBridge.send(postViewBindingName, message);

    return savedPost;
  }

  /**
   * Get batch posts by id.
   * @param postIds post ids
   * @return posts
   * */
  public List<Post> getBatchPostsById (List<String> postIds) {
    return postRepository.findAllById(postIds);
  }

  /**
   * Create post. Only verified users can create posts.
   * @param post post
   * @param creatorId creator id
   * @param creatorRole creator role
   * @return post
   */

  public Post createPost(Post post, String creatorId, String creatorRole) {
    if(creatorRole.equals(Role.UNVERIFIED.name())){
      throw new NotAuthorizedException("Only verified users can create posts");
    }
    //todo: ask for full name
    post.setUserId(creatorId);
    post.setCreatedAt(LocalDateTime.now());
    post.setUpdatedAt(LocalDateTime.now());
    return postRepository.save(post);
  }

  /**
   * Update post. Only post owner can update post.
   * Can only update title, content, isArchived.
   * @param postId post id
   * @param post post
   * @param updaterId updater id
   * @param updaterRole updater role
   * @return post
   * */

  public Post updatePost(String postId, Post post, String updaterId, String updaterRole) {
    Post existedpost = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    if(!existedpost.getUserId().equals(updaterId)){
      throw new NotAuthorizedException("Only post owner can update post");
    }
    if(updaterRole.equals(Role.UNVERIFIED.name())){
      throw new NotAuthorizedException("Only verified users can update posts");
    }
    existedpost.setTitle(post.getTitle());
    existedpost.setContent(post.getContent());
    existedpost.setUpdatedAt(LocalDateTime.now());
    return postRepository.save(existedpost);
  }

  /**
   * Reply to a post / reply depending on replyId is null or not.
   * @param postId post id
   * @param replyId reply id
   * @param comment comment
   * @param replierId replier id
   * @param replierRole replier role
   * @return post
   * */
  public Post replyPost(String postId, String replyId, String comment, String replierId, String replierRole) {
    if(replierRole.equals(Role.UNVERIFIED.name())){
      throw new NotAuthorizedException("Only verified users can reply posts");
    }
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));

    // If replyId exist, it is a sub-reply to a reply
    if(replyId != null){
      Reply reply = post.getReplies().stream()
          .filter(r -> r.getId().equals(replyId))
          .findFirst()
          .orElseThrow(() -> new NotFoundException("Reply not found"));
      SubReply subReply = SubReply.builder()
          .comment(comment)
          .userId(replierId)
          .createdAt(LocalDateTime.now())
          .build();
      reply.getSubReplies().add(subReply);
    }else{
      // If replyId is null, it is a reply to the post
      Reply reply = Reply.builder()
          .comment(comment)
          .userId(replierId)
          .createdAt(LocalDateTime.now())
          .build();
      post.getReplies().add(reply);
    }
    postRepository.save(post);
    return post;
  }

  public Post transferPostStatus(String postId, String operationStr, String updaterId, String updaterRoleStr) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    Operation operation;
    try {
      operation = Operation.valueOf(operationStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new NotFoundException("Operation not found");
    }
    PostStatus currentStatus = PostStatus.valueOf(post.getStatus());
    Role updaterRole = Role.valueOf(updaterRoleStr.toUpperCase());
    switch (operation) {
      case BAN:
        if (updaterRole != Role.ADMIN && updaterRole != Role.SUPER_ADMIN) {
          throw new NotAuthorizedException("Only admin or super admin can ban posts");
        }
        // Allow banning if current status is BANNED (idempotent) or PUBLISHED
        if (currentStatus == PostStatus.BANNED || currentStatus == PostStatus.PUBLISHED) {
          post.setStatus(PostStatus.BANNED.name());
        } else {
          throw new IllegalArgumentException("Cannot ban post from status: " + currentStatus);
        }
        break;

      case UNBAN:
        if (updaterRole != Role.ADMIN && updaterRole != Role.SUPER_ADMIN) {
          throw new NotAuthorizedException("Only admin or super admin can unban posts");
        }
        // Only unban if currently BANNED
        if (currentStatus == PostStatus.BANNED) {
          post.setStatus(PostStatus.PUBLISHED.name());
        } else {
          throw new IllegalArgumentException("Cannot unban post from status: " + currentStatus);
        }
        break;

      case HIDE:
        if (!updaterId.equals(post.getUserId())) {
          throw new NotAuthorizedException("Only post owner can hide posts");
        }
        // Allow hiding if current status is HIDDEN (idempotent) or PUBLISHED
        if (currentStatus == PostStatus.HIDDEN || currentStatus == PostStatus.PUBLISHED) {
          post.setStatus(PostStatus.HIDDEN.name());
        } else {
          throw new IllegalArgumentException("Cannot hide post from status: " + currentStatus);
        }
        break;

      case SHOW:
        if (!updaterId.equals(post.getUserId())) {
          throw new NotAuthorizedException("Only post owner can show posts");
        }
        // Only show if currently HIDDEN
        if (currentStatus == PostStatus.HIDDEN) {
          post.setStatus(PostStatus.PUBLISHED.name());
        } else {
          throw new IllegalArgumentException("Cannot show post from status: " + currentStatus);
        }
        break;

      case DELETE:
        if (!updaterId.equals(post.getUserId()) && updaterRole != Role.ADMIN && updaterRole != Role.SUPER_ADMIN) {
          throw new NotAuthorizedException("Only post owner, admin, or super admin can delete posts");
        }
        // Allow deletion from any state
        if (currentStatus != PostStatus.DELETED) {
          post.setStatus(PostStatus.DELETED.name());
        } else {
          throw new IllegalArgumentException("Post " + postId + " is already in DELETED status.");
        }
        break;

      case RECOVER:
        if (updaterRole != Role.ADMIN && updaterRole != Role.SUPER_ADMIN) {
          throw new NotAuthorizedException("Only admin or super admin can recover posts");
        }
        // Only recover if currently DELETED
        if (currentStatus == PostStatus.DELETED) {
          post.setStatus(PostStatus.PUBLISHED.name()); // Recover automatically moves it back to Published
        } else {
          throw new IllegalArgumentException("Cannot recover post from status: " + currentStatus);
        }
        break;

      default:
        throw new IllegalArgumentException("Unsupported operation: " + operation);
    }
    return postRepository.save(post);
  }

  public Post toggleReplyActive(String postId, String replyId, String subReplyId, String updaterId, String updaterRole) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new NotFoundException("Post not found"));
    Reply reply = post.getReplies().stream()
        .filter(r -> r.getId().equals(replyId))
        .findFirst()
        .orElseThrow(() -> new NotFoundException("Reply not found"));
    if (subReplyId != null) {
      SubReply subReply = reply.getSubReplies().stream()
          .filter(sr -> sr.getId().equals(subReplyId))
          .findFirst()
          .orElseThrow(() -> new NotFoundException("SubReply not found"));
      if (!updaterId.equals(subReply.getUserId()) &&
          !updaterId.equals(post.getUserId()) &&
          !updaterRole.equals(Role.ADMIN.name()) &&
          !updaterRole.equals(Role.SUPER_ADMIN.name())){
        throw new NotAuthorizedException("Only reply owner, post owner and admin can change reply visibility");
      }
      subReply.setIsActive(!subReply.getIsActive());
    } else {
      if (!updaterId.equals(reply.getUserId()) &&
          !updaterId.equals(post.getUserId()) &&
          !updaterRole.equals(Role.ADMIN.name()) &&
          !updaterRole.equals(Role.SUPER_ADMIN.name())){
        throw new NotAuthorizedException("Only reply owner, post owner and admin can change reply visibility");
      }
      reply.setIsActive(!reply.getIsActive());
    }
    return postRepository.save(post);
  }

  /**
   * Get posts by query parameters.
   * @param page page number
   * @param size page size
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param status post status
   * @param keyword search keyword
   * @param searchIn search in
   * @param userId user id
   * @param viewerId viewer id
   * @param viewerRole viewer role
   * @return posts
   * */
  @Transactional(readOnly = true)
  public Page<Post> getQueriedPosts(int page, int size,
                                   String sortBy, String sortDir,
                                   String status, String keyword,
                                   String searchIn, String userId,
                                   String viewerId, String viewerRole) {
    if(viewerRole.equals(Role.UNVERIFIED.name())){
      throw new NotAuthorizedException("Only verified users can view posts");
    }
    // Sorting and pagination
    Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
    Sort sort = Sort.by(direction, sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);

    // find by status
    if (PostStatus.isPostStatus(status)) {
      //todo: should be more authority confirmed.
      return postRepository.findByStatus(status, pageable);
    }

    // search by keyword
    if (Objects.nonNull(keyword) && Objects.nonNull(searchIn)) {
      switch (searchIn) {
        case "title" -> {
          return postRepository.findByTitleContainingAndPublished(keyword, pageable);
        }
        case "content" -> {
          return postRepository.findByContentContainingAndPublished(keyword, pageable);
        }
        case "author" -> {
          String regex = ".*" + Pattern.quote(keyword) + ".*";
          return postRepository.findByFullNameRegex(regex, pageable);
        }
        default -> {
          return Page.empty();
        }
      }
    }

    // find by userId
    if (Objects.nonNull(userId)) {
      return postRepository.findByUserIdAndPublished(userId, pageable);
    }

    // find all
    return postRepository.findAllPublished(pageable);
  }


}
