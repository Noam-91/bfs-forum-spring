package com.bfsforum.postservice.service;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.domain.Reply;
import com.bfsforum.postservice.domain.SubReply;
import com.bfsforum.postservice.domain.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Transactional
@Service
@Slf4j
public class UserInfoAggregator {
    private final StreamBridge streamBridge;
    private final RequestReplyManager<List<UserInfo>> requestReplyManager;
    public UserInfoAggregator(StreamBridge streamBridge, RequestReplyManager<List<UserInfo>> requestReplyManager) {
        this.streamBridge = streamBridge;
        this.requestReplyManager = requestReplyManager;
    }

    @Value("${app.kafka.topics.user-info-request}")
    private String userInfoRequestBindingName;

    public List<Post> fetchAndIntegrateAllPostsUserInfo(List<Post> posts) {
        // 1. Scratch user IDs
        Set<String> userIdsToFetch = scratchUserIdsFromPostsAndReplies(posts);
        log.debug("Scratch user IDs: {}", userIdsToFetch);

        // 2. Fetch batch UserInfo
        List<UserInfo> fetchedUserInfos = getBatchUserInfo(userIdsToFetch);
        log.debug("Fetched UserInfos: {}", fetchedUserInfos);

        // 3. Integrate UserInfo
        return integrateUserInfoOnPosts(posts, fetchedUserInfos);
    }

    private Set<String> scratchUserIdsFromPostsAndReplies(List<Post> posts){
        Set<String> userIds = new HashSet<>();
        for(Post post : posts){
            userIds.add(post.getUserInfo().getUserId());
            for (Reply reply : post.getReplies()) {
                userIds.add(reply.getUserInfo().getUserId());
                for (SubReply subReply : reply.getSubReplies()) {
                    userIds.add(subReply.getUserInfo().getUserId());
                }
            }
        }
        return userIds;
    }

    private List<UserInfo> getBatchUserInfo(Set<String> userIds) {
        if (userIds.isEmpty()) {
            return List.of();
        }
        String correlationId = UUID.randomUUID().toString();

        CompletableFuture<List<UserInfo>> future = requestReplyManager.createAndStoreFuture(correlationId);
        Message<Set<String>> message = MessageBuilder.withPayload(userIds)
            .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
            .build();
        log.debug("Sending userInfo request: {} to kafka topic: {}", message, userInfoRequestBindingName);
        streamBridge.send(userInfoRequestBindingName, message);

        return requestReplyManager.awaitFuture(correlationId, future);
    }

    private List<Post> integrateUserInfoOnPosts(List<Post> posts, List<UserInfo> userInfos){
        if (posts == null || posts.isEmpty() || userInfos == null || userInfos.isEmpty()) {
            log.warn("Either posts or userInfos list is null or empty. No integration will occur.");
            return posts;
        }
        Map<String, UserInfo> userInfoMap = userInfos.stream()
            .collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
        for (Post post : posts) {
            // Update Post's UserInfo
            if (post.getUserInfo() != null && post.getUserInfo().getUserId() != null) {
                String userId = post.getUserInfo().getUserId();
                UserInfo fullUserInfo = userInfoMap.get(userId);
                if (fullUserInfo != null) {
                    post.setUserInfo(fullUserInfo);
                } else {
                    log.warn("Full UserInfo not found for userId: {} in Post ID: {}", userId, post.getId());
                }
            }

            // Update Replies' UserInfo
            if (post.getReplies() != null) {
                for (Reply reply : post.getReplies()) {
                    if (reply.getUserInfo() != null && reply.getUserInfo().getUserId() != null) {
                        String userId = reply.getUserInfo().getUserId();
                        UserInfo fullUserInfo = userInfoMap.get(userId);
                        if (fullUserInfo != null) {
                            reply.setUserInfo(fullUserInfo);
                        } else {
                            log.warn("Full UserInfo not found for userId: {} in Reply ID: {}", userId, reply.getId());
                        }
                    }

                    // Update SubReplies' UserInfo
                    if (reply.getSubReplies() != null) {
                        for (SubReply subReply : reply.getSubReplies()) {
                            if (subReply.getUserInfo() != null && subReply.getUserInfo().getUserId() != null) {
                                String userId = subReply.getUserInfo().getUserId();
                                UserInfo fullUserInfo = userInfoMap.get(userId);
                                if (fullUserInfo != null) {
                                    subReply.setUserInfo(fullUserInfo);
                                } else {
                                    log.warn("Full UserInfo not found for userId: {} in SubReply ID: {}", userId, subReply.getId());
                                }
                            }
                        }
                    }
                }
            }
        }
        return posts;
    }
}
