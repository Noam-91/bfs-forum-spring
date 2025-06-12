package com.bfsforum.postservice.service;

import com.bfsforum.postservice.dao.PostRepository;
import com.bfsforum.postservice.domain.*;
import com.bfsforum.postservice.dto.StatsDto;
import com.bfsforum.postservice.dto.StatusCountProjection;
import com.bfsforum.postservice.exception.NotAuthorizedException;
import com.bfsforum.postservice.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class PostService {
    private final PostRepository postRepository;
    private final StreamBridge streamBridge;
    private final UserInfoAggregator userInfoAggregator;
    private final MongoTemplate mongoTemplate;

    @Value("${app.kafka.topics.post-view-notification}")
    private String postViewBindingName;

    public PostService(PostRepository postRepository, StreamBridge streamBridge,
                       UserInfoAggregator userInfoAggregator, MongoTemplate mongoTemplate) {
        this.postRepository = postRepository;
        this.streamBridge = streamBridge;
        this.userInfoAggregator = userInfoAggregator;
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Get post by id.
     * increment view count.
     * send view event to kafka.
     *
     * @param postId post id
     * @param userId user id
     * @return post
     */
    public Post getPostById(String postId, String userId) {
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        if (!post.getStatus().equals(PostStatus.PUBLISHED.name()) && !post.getUserInfo().getUserId().equals(userId)) {
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
     *
     * @param postIds post ids
     * @return posts
     */
    public List<Post> getBatchPostsById(List<String> postIds) {
        List<Post> posts = postRepository.findAllById(postIds);

        // Aggregate UserInfo
        posts = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(posts);

        return posts;
    }

    /**
     * Create post. Only verified users can create posts.
     *
     * @param post        post
     * @param creatorId   creator id
     * @param creatorRole creator role
     * @return post
     */

    public Post createPost(Post post, String creatorId, String creatorRole) {
        if (creatorRole.equals(Role.UNVERIFIED.name())) {
            throw new NotAuthorizedException("Only verified users can create posts");
        }
        post.setUserInfo(new UserInfo(creatorId));
        post.setStatus(post.getStatus());
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        // Integrate UserInfo
        savedPost = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(List.of(savedPost)).get(0);
        return savedPost;
    }

    /**
     * Update post. Only post owner can update post.
     * Can only update title, content, isArchived.
     *
     * @param postId      post id
     * @param post        post
     * @param updaterId   updater id
     * @param updaterRole updater role
     * @return post
     */

    public Post updatePost(String postId, Post post, String updaterId, String updaterRole) {
        Post existedpost = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));
        if (!existedpost.getUserInfo().getUserId().equals(updaterId)) {
            throw new NotAuthorizedException("Only post owner can update post");
        }
        if (updaterRole.equals(Role.UNVERIFIED.name())) {
            throw new NotAuthorizedException("Only verified users can update posts");
        }
        existedpost.setTitle(post.getTitle());
        existedpost.setContent(post.getContent());
        existedpost.setUpdatedAt(LocalDateTime.now());

        Post savedPost = postRepository.save(existedpost);
        // Integrate UserInfo
        savedPost = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(List.of(savedPost)).get(0);
        return savedPost;
    }

    /**
     * Reply to a post / reply depending on replyId is null or not.
     *
     * @param postId      post id
     * @param replyId     reply id
     * @param comment     comment
     * @param replierId   replier id
     * @param replierRole replier role
     * @return post
     */
    public Post replyPost(String postId, String replyId, String comment, String replierId, String replierRole) {
        if (replierRole.equals(Role.UNVERIFIED.name())) {
            throw new NotAuthorizedException("Only verified users can reply posts");
        }

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new NotFoundException("Post not found"));

        if (post.getStatus().equals(PostStatus.ARCHIVED.name())) {
            throw new IllegalArgumentException("Cannot reply to an archived post");
        }

        // If replyId exist, it is a sub-reply to a reply
        if (replyId != null) {
            Reply reply = post.getReplies().stream()
                .filter(r -> r.getId().equals(replyId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Reply not found"));
            SubReply subReply = SubReply.builder()
                .comment(comment)
                .userInfo(new UserInfo(replierId))
                .createdAt(LocalDateTime.now())
                .build();
            reply.getSubReplies().add(subReply);
        } else {
            // If replyId is null, it is a reply to the post
            Reply reply = Reply.builder()
                .comment(comment)
                .userInfo(new UserInfo(replierId))
                .createdAt(LocalDateTime.now())
                .build();
            post.getReplies().add(reply);
        }

        // replyCount increment
        post.setReplyCount(post.getReplyCount() + 1);

        Post savedPost = postRepository.save(post);
        // Integrate UserInfo
        savedPost = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(List.of(savedPost)).get(0);
        return savedPost;
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
                if (!updaterId.equals(post.getUserInfo().getUserId())) {
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
                if (!updaterId.equals(post.getUserInfo().getUserId())) {
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
                if (!updaterId.equals(post.getUserInfo().getUserId()) && updaterRole != Role.ADMIN && updaterRole != Role.SUPER_ADMIN) {
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

            case ARCHIVE:
                if (!updaterId.equals(post.getUserInfo().getUserId())) {
                    throw new NotAuthorizedException("Only post owner can archive posts");
                }
                // Allow archiving if current status is ARCHIVED (idempotent) or PUBLISHED
                if (currentStatus == PostStatus.ARCHIVED || currentStatus == PostStatus.PUBLISHED) {
                    post.setStatus(PostStatus.ARCHIVED.name());
                } else {
                    throw new IllegalArgumentException("Cannot archive post from status: " + currentStatus);
                }
                break;

            case UNARCHIVE:
                if (!updaterId.equals(post.getUserInfo().getUserId())) {
                    throw new NotAuthorizedException("Only post owner can unarchive posts");
                }
                // Only unarchive if currently ARCHIVED
                if (currentStatus == PostStatus.ARCHIVED) {
                    post.setStatus(PostStatus.PUBLISHED.name());
                }
                break;

            default:
                throw new IllegalArgumentException("Unsupported operation: " + operation);
        }

        post.setUpdatedAt(LocalDateTime.now());
        Post savedPost = postRepository.save(post);
        // Integrate UserInfo
        savedPost = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(List.of(savedPost)).get(0);
        return savedPost;
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
            if (!updaterId.equals(subReply.getUserInfo().getUserId()) &&
                !updaterId.equals(post.getUserInfo().getUserId()) &&
                !updaterRole.equals(Role.ADMIN.name()) &&
                !updaterRole.equals(Role.SUPER_ADMIN.name())) {
                throw new NotAuthorizedException("Only reply owner, post owner and admin can change reply visibility");
            }
            subReply.setIsActive(!subReply.getIsActive());
            // replyCount change
            int countChange = subReply.getIsActive() ? 1 : -1;
            post.setReplyCount(post.getReplyCount() + countChange);
        } else {
            if (!updaterId.equals(reply.getUserInfo().getUserId()) &&
                !updaterId.equals(post.getUserInfo().getUserId()) &&
                !updaterRole.equals(Role.ADMIN.name()) &&
                !updaterRole.equals(Role.SUPER_ADMIN.name())) {
                throw new NotAuthorizedException("Only reply owner, post owner and admin can change reply visibility");
            }
            reply.setIsActive(!reply.getIsActive());
            // replyCount change
            int countChange = reply.getIsActive() ? 1 : -1;
            post.setReplyCount(post.getReplyCount() + countChange);
        }
        Post savedPost = postRepository.save(post);
        // Integrate UserInfo
        savedPost = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(List.of(savedPost)).get(0);
        return savedPost;
    }

    /**
     * Get posts by query parameters.
     *
     * @param page       page number
     * @param size       page size
     * @param sortBy     sort by
     * @param sortDir    sort direction
     * @param status     post status
     * @param keyword    search keyword
     * @param searchIn   search in
     * @param userId     user id
     * @param viewerId   viewer id
     * @param viewerRole viewer role
     * @return posts
     */
    @Transactional(readOnly = true)
    public Page<Post> getQueriedPosts(int page, int size,
                                      String sortBy, String sortDir,
                                      String status, String keyword,
                                      String searchIn, String userId,
                                      String viewerId, String viewerRole) {
        if (viewerRole.equals(Role.UNVERIFIED.name())) {
            throw new NotAuthorizedException("Only verified users can view posts");
        }

        Query query = new Query();

        // Filter by status
        if (PostStatus.isPostStatus(status)) {
            // TODO: Implement more authority confirmation here if needed
            query.addCriteria(Criteria.where("status").is(status));
        } else {
            // Default to only published posts if no specific status is requested
            query.addCriteria(Criteria.where("status").is(PostStatus.PUBLISHED.name()));
        }

        // Search by keyword in title, content
        if (Objects.nonNull(keyword) && Objects.nonNull(searchIn)) {
            switch (searchIn.toLowerCase()) {
                case "title" ->
                    // $regex is often case-sensitive by default, use i option for case-insensitivity
                    query.addCriteria(Criteria.where("title").regex(Pattern.quote(keyword), "i"));

                case "content" ->
                    query.addCriteria(Criteria.where("content").regex(Pattern.quote(keyword), "i"));

                default ->
                    log.warn("Invalid searchIn parameter: {}. Ignoring keyword search.", searchIn);

            }
        }

        // Filter by specific userId
        if (Objects.nonNull(userId)) {
            // Assuming userInfo.userId is the field in Post document
            // Assume published for general users unless status is explicitly provided
            query.addCriteria(Criteria.where("userInfo.userId").is(userId));
        }

        // Apply Sorting and Pagination
        Sort.Direction direction = sortDir.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        query.with(pageable);
        query.with(sort);

        // fetch from DB
        List<Post> posts = mongoTemplate.find(query, Post.class);
        long total = mongoTemplate.count(query, Post.class);

        // integrate userInfo
        posts = userInfoAggregator.fetchAndIntegrateAllPostsUserInfo(posts);

        return new PageImpl<>(posts, pageable, total);
    }

    /**
     * Calculates and returns statistics about post statuses in the form of a StatsDto.
     *
     * @return A StatsDto containing total post count and counts for each status.
     */
    public StatsDto getPostStats(String userId, String userRole) {
        // Check if the user is authorized to view stats
        if (!userRole.equals(Role.ADMIN.name())&&!userRole.equals(Role.SUPER_ADMIN.name())) {
            throw new NotAuthorizedException("Only admin or super admin can view post stats");
        }

        Map<PostStatus, Long> counts = getPostStatusCounts();

        // Sum up all counts to get the total
        Integer total = counts.values().stream()
            .mapToInt(Long::intValue)
            .sum();

        // Build the StatsDto using the retrieved counts and the total
        return StatsDto.builder()
            .total(total)
            .UnpublishedCount(counts.getOrDefault(PostStatus.UNPUBLISHED, 0L).intValue())
            .PublishedCount(counts.getOrDefault(PostStatus.PUBLISHED, 0L).intValue())
            .HiddenCount(counts.getOrDefault(PostStatus.HIDDEN, 0L).intValue())
            .BannedCount(counts.getOrDefault(PostStatus.BANNED, 0L).intValue())
            .ArchivedCount(counts.getOrDefault(PostStatus.ARCHIVED, 0L).intValue())
            .DeletedCount(counts.getOrDefault(PostStatus.DELETED, 0L).intValue())
            .build();
    }

    private Map<PostStatus, Long> getPostStatusCounts() {
        // Now calling the MongoDB-specific repository method
        List<StatusCountProjection> statusCounts = postRepository.countPostsByStatus();

        // Convert the List<StatusCountProjection> into a Map<PostStatus, Long>
        Map<PostStatus, Long> countsMap = statusCounts.stream()
            .collect(Collectors.toMap(
                StatusCountProjection::get_id, // Key: PostStatus enum from _id field
                StatusCountProjection::getCount  // Value: count
            ));

        // Optional: Ensure all PostStatus enums are present in the map, even if their count is 0
        Map<PostStatus, Long> fullCountsMap = new EnumMap<>(PostStatus.class);
        Arrays.stream(PostStatus.values()).forEach(status ->
            fullCountsMap.put(status, countsMap.getOrDefault(status, 0L))
        );

        return fullCountsMap;
    }
}
