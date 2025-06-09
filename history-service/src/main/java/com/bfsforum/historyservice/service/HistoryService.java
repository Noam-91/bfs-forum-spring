package com.bfsforum.historyservice.service;

import com.bfsforum.historyservice.domain.History;
import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.dto.PostDto;
import com.bfsforum.historyservice.repository.HistoryRepo;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.Page;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Qualifier;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
public class HistoryService {

    private final HistoryRepo historyRepo;
    private final StreamBridge streamBridge;
    private final RequestReplyManager<List<Post>> requestReplyManager;

    public HistoryService(HistoryRepo historyRepo, StreamBridge streamBridge, @Qualifier("postListManager") RequestReplyManager<List<Post>> requestReplyManager ) {
        this.historyRepo = historyRepo;
        this.streamBridge = streamBridge;
        this.requestReplyManager = requestReplyManager;
    }

    @Value("${bfs-forum.kafka.request-binding-name}") //Custom value
    private String requestBindingName;
    /** A wrapper only for test purpose of jap findByUserIdAndPostId method
     */
    public Optional<History> getByUserAndPost(UUID userId, UUID postId) {
        return historyRepo.findByUserIdAndPostId(userId, postId);
    }



    /**
     * Write a history record in DB every time consumes a PostViewedEvent
     */
    // evict the cache every time when new record is created
    @CacheEvict(cacheNames = "history", key = "#userId")
    public History recordView(UUID userId, UUID postId) {
        LocalDateTime now = LocalDateTime.now();
        return historyRepo.findByUserIdAndPostId(userId, postId)
                .map(h -> {
                    h.setViewedAt(now);
                    return historyRepo.save(h);
                })
                .orElseGet(() -> {
                    History h = History.builder().userId(userId).postId(postId).viewedAt(now).build();
                    return historyRepo.save(h);
                });
    }


    /**
     * Fetches raw history from the DB, asks the post-service for full PostDto details via Kafka,
     * then merges into EnrichedHistoryDto.
     */
    // cache implemented to reuse the enriched history results for the next 2 filter functionalities
    @Cacheable(cacheNames = "history", key = "#userId")
    public List<EnrichedHistoryDto> loadFullEnrichedHistory(UUID userId) {
        // 1) raw history list to postId list
        List<History> raw = historyRepo.findByUserIdOrderByViewedAtDesc(userId);
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }
        List<UUID> postIds = raw.stream().map(History::getPostId).collect(Collectors.toList());

        // 2) build & send enrichment request
        String correlationId = UUID.randomUUID().toString();
            // a. create a CompletableFuture and store it
        CompletableFuture<List<Post>> future = requestReplyManager.createAndStoreFuture(correlationId);
            // b. prepare and send the Kafka request message with correlationId in header
        Message<List<UUID>> message = MessageBuilder.withPayload(postIds)
                .setHeader(KafkaHeaders.CORRELATION_ID, correlationId)
                .build();
        streamBridge.send(requestBindingName, message);
            // c. wait for the future to complete with a timeout
        List<Post> repliedPosts = requestReplyManager.awaitFuture(correlationId, future);
//        PostsEnrichmentRequest req =
//                new PostsEnrichmentRequest(UUID.randomUUID().toString(), postIds);
//        ProducerRecord<String, PostsEnrichmentRequest> record =
//                new ProducerRecord<>("posts-enrichment-request", req);
//        PostsEnrichmentResponse resp;
//        try {
//            // send req and register for a reply
//            RequestReplyFuture<String, PostsEnrichmentRequest, PostsEnrichmentResponse> future = kafka.sendAndReceive(record);
//            // block up to 5s until reply arrives
//            ConsumerRecord<String, PostsEnrichmentResponse> cr = future.get(5, TimeUnit.SECONDS);
//            // extract the DTO from record
//            resp = cr.value();
//        } catch (Exception ex) {
//            throw new RuntimeException("Failed for getting response from post-service: " + userId, ex);
//        }

        // 3) merge into EnrichedHistoryDto
        // build lookup table for PostDto (key: postId, val: PostDto)
        Map<UUID, PostDto> postsById = repliedPosts
                .stream()
                .collect(Collectors.toMap(Post::getId, p -> PostDto.builder().postId(p.getId()).title(p.getTitle()).content(p.getContent()).build()));
        // transform raw history to enriched Dtos
        return raw.stream()
                .filter(h-> postsById.containsKey(h.getPostId()))
                .map(h -> EnrichedHistoryDto.
                        builder().
                        postId(h.getPostId()).
                        viewedAt(h.getViewedAt()).
                        post(postsById.get(h.getPostId()))
                        .build()
                )
                .collect(Collectors.toList());
    }

    public static <T> Page<T> toPage(List<T> list, Pageable pageable) {
        int total = list.size();
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), total);
        List<T> slice = (start > end)
                ? Collections.emptyList()
                : list.subList(start, end);
        return new PageImpl<>(slice, pageable, total);
    }

    public Page<EnrichedHistoryDto> getEnrichedHistory(
            UUID userId, Pageable pageable) {
        HistoryService proxy = (HistoryService) AopContext.currentProxy();
        List<EnrichedHistoryDto> full = proxy.loadFullEnrichedHistory(userId);
        return toPage(full, pageable);

    }
    /**
     * Fetch full enriched history then filter by keyword (in title or content)
     */
    public Page<EnrichedHistoryDto> searchByKeyword(UUID userId, String keyword, Pageable pageable) {
        try {
            String lower = keyword.toLowerCase();
            HistoryService proxy = (HistoryService) AopContext.currentProxy();
            List<EnrichedHistoryDto> filtered = proxy.loadFullEnrichedHistory(userId).stream()
                    .filter(dto -> {
                        PostDto p = dto.getPost();
                        return (p.getTitle()   != null && p.getTitle().toLowerCase().contains(lower))
                                || (p.getContent() != null && p.getContent().toLowerCase().contains(lower));
                    })
                    .collect(Collectors.toList());
            return toPage(filtered,pageable);

        } catch (Exception ex) {
            throw new RuntimeException("Failed for search by keyword: " + userId, ex);
        }
    }

    /**
     * Filter cached enriched history by bonded startDate and endDate.
     */
    public Page<EnrichedHistoryDto> searchByDate(UUID userId, LocalDate startDate, LocalDate endDate, Pageable pageable) {
        try {
            HistoryService proxy = (HistoryService) AopContext.currentProxy();
            List<EnrichedHistoryDto> filtered = proxy.loadFullEnrichedHistory(userId).stream()
                    .filter(dto -> {
                        LocalDate viewed = dto.getViewedAt().toLocalDate();
                        return (!viewed.isBefore(startDate))
                                && (!viewed.isAfter(endDate));
                    })
                    .collect(Collectors.toList());
            return toPage(filtered, pageable);
        } catch (Exception ex) {
            throw new RuntimeException(
                    String.format("Failed for date range search [%s – %s] for user %s",
                            startDate, endDate, userId), ex);
        }
    }
}
