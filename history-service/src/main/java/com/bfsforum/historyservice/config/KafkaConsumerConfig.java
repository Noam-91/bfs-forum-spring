package com.bfsforum.historyservice.config;

import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.dto.PostDto;
import com.bfsforum.historyservice.repository.HistoryRepo;
import com.bfsforum.historyservice.service.HistoryService;
import com.bfsforum.historyservice.service.RequestReplyManager;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

//@Configuration
//@Slf4j
//public class KafkaConsumerConfig {
//    // Beans for RequestReplyManager
//    @Bean(name = "singlePostManager")
//    public RequestReplyManager<Post> singlePostManager() {
//        return new RequestReplyManager<>();
//    }
//
//    @Bean(name = "postListManager")
//    public RequestReplyManager<List<Post>> postListManager() {
//        return new RequestReplyManager<>();
//    }
//    // Inject beans
//    private final RequestReplyManager<Post> singlePostManager;
//    private final RequestReplyManager<List<Post>> postListManager;
//    private final HistoryService historyService;
//
//    public KafkaConsumerConfig(
//            @Qualifier("singlePostManager") RequestReplyManager<Post> singlePostManager,
//            @Qualifier("postListManager")  RequestReplyManager<List<Post>> postListManager,
//            HistoryService historyService
//    ) {
//        this.singlePostManager = singlePostManager;
//        this.postListManager   = postListManager;
//        this.historyService    = historyService;
//    }
//    @Bean
//    public Consumer<Message<Post>> postNotificationEventConsumer() {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            Post post = message.getPayload();
//            // save the new view record into history db
//            log.info("Received post notification for correlationId {}: {}", correlationId, post);
//            singlePostManager.completeFuture(correlationId, post);
//
//            historyService.recordView(post.getUserId(),post.getId());
//        };
//    }
//    @Bean
//    public Consumer<Message<List<Post>>> postEnrichRequestEventConsumer() {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            List<Post> repliedPosts = message.getPayload();
//
//            log.info("Received replied post list for correlationId {}: {}", correlationId, repliedPosts);
//            postListManager.completeFuture(correlationId, repliedPosts);
//        };
//    }
//    @Bean
//    public Consumer<Message<Post>> postNotificationEventConsumer(
//            RequestReplyManager<Post> singlePostManager,
//            HistoryService historyService
//    ) {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            Post post = message.getPayload();
//            log.info("Received post notification for correlationId {}: {}", correlationId, post);
//
//            // Complete any waiting future
//            singlePostManager.completeFuture(correlationId, post);
//
//            // Persist view record
//            historyService.recordView(post.getUserId(), post.getId());
//        };
//    }
//
//    /**
//     * Consumer for list-of-Post enrichment replies.
//     */
//    @Bean
//    public Consumer<Message<List<Post>>> postEnrichRequestEventConsumer(
//            RequestReplyManager<List<Post>> postListManager
//    ) {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            List<Post> posts = message.getPayload();
//            log.info("Received replied post list for correlationId {}: {}", correlationId, posts);
//
//            postListManager.completeFuture(correlationId, posts);
//        };
//    }
//}
//@Configuration
//@Slf4j
//public class KafkaConsumerConfig {
//
//    // --- RequestReplyManager Beans ---
//
//    @Bean("singlePostManager")
//    public RequestReplyManager<Post> singlePostManager() {
//        return new RequestReplyManager<>();
//    }
//
//    @Bean("postListManager")
//    public RequestReplyManager<List<Post>> postListManager() {
//        return new RequestReplyManager<>();
//    }
//
//    // --- Kafka Consumer Beans ---
//
//    /**
//     * Consumer for single-Post notifications (1:1 reply).
//     */
//    @Bean
//    public Consumer<Message<Post>> postNotificationEventConsumer(
//            @Qualifier("singlePostManager") RequestReplyManager<Post> singlePostManager,
//            HistoryService historyService
//    ) {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            Post post = message.getPayload();
//            log.info("Received post notification for correlationId {}: {}", correlationId, post);
//
//            // Complete the waiting future
//            singlePostManager.completeFuture(correlationId, post);
//
//            // Persist the view record
//            historyService.recordView(post.getUserId(), post.getId());
//        };
//    }
//
//    /**
//     * Consumer for list-of-Post enrichment replies.
//     */
//    @Bean
//    public Consumer<Message<List<Post>>> postEnrichRequestEventConsumer(
//            @Qualifier("postListManager") RequestReplyManager<List<Post>> postListManager
//    ) {
//        return message -> {
//            String correlationId = (String) message.getHeaders().get(KafkaHeaders.CORRELATION_ID);
//            List<Post> posts = message.getPayload();
//            log.info("Received post list reply for correlationId {}: {}", correlationId, posts);
//
//            // Complete the waiting future
//            postListManager.completeFuture(correlationId, posts);
//        };
//    }
//}