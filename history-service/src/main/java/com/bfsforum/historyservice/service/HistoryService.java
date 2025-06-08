package com.bfsforum.historyservice.service;

import com.bfsforum.historyservice.domain.History;
import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.dto.PostDto;
import com.bfsforum.historyservice.repository.HistoryRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.aop.framework.AopContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;
import com.bfsforum.historyservice.kafka.event.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class HistoryService {

    private final HistoryRepo historyRepo;
    private final ReplyingKafkaTemplate<String, PostsEnrichmentRequest, PostsEnrichmentResponse> kafka;

    public HistoryService(HistoryRepo historyRepo, ReplyingKafkaTemplate<String, PostsEnrichmentRequest, PostsEnrichmentResponse> kafka) {
        this.historyRepo = historyRepo;
        this.kafka = kafka;
    }

    /** A wrapper only for test purpose of jap findByUserIdAndPostId method
     */
    public Optional<History> getByUserAndPost(String userId, String postId) {
        return historyRepo.findByUserIdAndPostId(userId, postId);
    }

    /**
     * Write a history record in DB every time consumes a PostViewedEvent
     */
    // evict the cache every time when new record is created
    @CacheEvict(cacheNames = "history", key = "#userId")
    public History recordView(String userId, String postId, LocalDateTime viewedAt) {
        return historyRepo.findByUserIdAndPostId(userId, postId)
                .map(h -> {
                    h.setViewedAt(viewedAt);
                    return historyRepo.save(h);
                })
                .orElseGet(() -> {
                    History h = History.builder().userId(userId).postId(postId).viewedAt(viewedAt).build();
                    return historyRepo.save(h);
                });
    }

    /**
     * Fetches raw history from the DB, asks the post-service for full PostDto details via Kafka,
     * then merges into EnrichedHistoryDto.
     */
    // cache implemented to reuse the enriched history results for the next 2 filter functionalities
    @Cacheable(cacheNames = "history", key = "#userId")
    public List<EnrichedHistoryDto> loadFullEnrichedHistory(String userId) {
        // 1) raw history
        List<History> raw = historyRepo.findByUserIdOrderByViewedAtDesc(userId);
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) build & send enrichment request
        List<String> postIds = raw.stream()
                .map(History::getPostId)
                .collect(Collectors.toList());
        PostsEnrichmentRequest req =
                new PostsEnrichmentRequest(UUID.randomUUID().toString(), postIds);
        ProducerRecord<String, PostsEnrichmentRequest> record =
                new ProducerRecord<>("posts-enrichment-request", req);
        PostsEnrichmentResponse resp;
        try {
            // send req and register for a reply
            RequestReplyFuture<String, PostsEnrichmentRequest, PostsEnrichmentResponse> future = kafka.sendAndReceive(record);
            // block up to 5s until reply arrives
            ConsumerRecord<String, PostsEnrichmentResponse> cr = future.get(5, TimeUnit.SECONDS);
            // extract the DTO from record
            resp = cr.value();
        } catch (Exception ex) {
            throw new RuntimeException("Failed for getting response from post-service: " + userId, ex);
        }

        // 3) merge into EnrichedHistoryDto
        // build lookup table for PostDto (key: postId, val: PostDto)
        Map<String, PostDto> postsById = resp.getPosts()
                .stream()
                .collect(Collectors.toMap(PostDto::getPostId, p -> p));
        // transform raw history to enriched Dtos
        return raw.stream()
                .filter(h-> postsById.containsKey(h.getPostId()))
                .map(h -> new EnrichedHistoryDto(
                        h.getPostId(),
                        h.getViewedAt(),
                        postsById.get(h.getPostId())
                ))
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
            String userId, Pageable pageable) {
        HistoryService proxy = (HistoryService) AopContext.currentProxy();
        List<EnrichedHistoryDto> full = proxy.loadFullEnrichedHistory(userId);
        return toPage(full, pageable);

    }
    /**
     * Fetch full enriched history then filter by keyword (in title or content)
     */
    public Page<EnrichedHistoryDto> searchByKeyword(String userId, String keyword, Pageable pageable) {
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
     * Filter cached enriched history by exact calendar date.
     */
    public Page<EnrichedHistoryDto> searchByDate(String userId, LocalDate date, Pageable pageable) {
        try {
            List<EnrichedHistoryDto> filtered = loadFullEnrichedHistory(userId).stream()
                    .filter(dto -> dto.getViewedAt().toLocalDate().equals(date))
                    .collect(Collectors.toList());
            return toPage(filtered,pageable);
        } catch (Exception ex) {
            throw new RuntimeException("Failed for search by date: " + userId, ex);
        }
    }
}
