package com.bfsforum.historyserivce.service;

import com.bfsforum.historyserivce.domain.History;
import com.bfsforum.historyserivce.dto.EnrichedHistoryDto;
import com.bfsforum.historyserivce.dto.PostDto;
import com.bfsforum.historyserivce.repository.HistoryRepo;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.kafka.requestreply.RequestReplyFuture;
import org.springframework.stereotype.Service;
import com.bfsforum.historyserivce.kafka.event.*;


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
    public Optional<History> getByUserAndPost(UUID userId, UUID postId) {
        return historyRepo.findByUserIdAndPostId(userId, postId);
    }

    /**
     * Write a history record in DB every time consumes a PostViewedEvent
     */
    // evict the cache every time when new record is created
    @CacheEvict(cacheNames = "history", key = "#userId")
    public History recordView(UUID userId, UUID postId, LocalDateTime viewedAt) {
        return historyRepo.findByUserIdAndPostId(userId, postId)
                .map(h -> {
                    h.setViewedAt(viewedAt);
                    return historyRepo.save(h);
                })
                .orElseGet(() -> {
                    History h = new History(userId, postId, viewedAt);
                    return historyRepo.save(h);
                });
    }

    /**
     * Fetches raw history from the DB, asks the post-service for full PostDto details via Kafka,
     * then merges into EnrichedHistoryDto.
     */
    // cache implemented to reuse the enriched history results for the next 2 filter functionalities
    @Cacheable(cacheNames = "history", key = "#userId")
    public List<EnrichedHistoryDto> getEnrichedHistory(UUID userId) {
        // 1) raw history
        List<History> raw = historyRepo.findByUserIdOrderByViewedAtDesc(userId);
        if (raw.isEmpty()) {
            return Collections.emptyList();
        }

        // 2) build & send enrichment request
        List<UUID> postIds = raw.stream()
                .map(History::getPostId)
                .collect(Collectors.toList());
        PostsEnrichmentRequest req =
                new PostsEnrichmentRequest(UUID.randomUUID(), postIds);
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
        Map<UUID, PostDto> postsById = resp.getPosts()
                .stream()
                .collect(Collectors.toMap(PostDto::getPostId, p -> p));
        // transform raw history to enriched Dtos
        return raw.stream()
                .map(h -> new EnrichedHistoryDto(
                        h.getPostId(),
                        h.getViewedAt(),
                        postsById.get(h.getPostId())
                ))
                .collect(Collectors.toList());
    }
    /**
     * Fetch full enriched history then filter by keyword (in title or content)
     */
    public List<EnrichedHistoryDto> searchByKeyword(UUID userId, String keyword) {
        try {
            String lower = keyword.toLowerCase();
            return getEnrichedHistory(userId).stream()
                    .filter(dto -> {
                        PostDto p = dto.getPost();
                        return (p.getTitle()   != null && p.getTitle().toLowerCase().contains(lower))
                                || (p.getContent() != null && p.getContent().toLowerCase().contains(lower));
                    })
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            throw new RuntimeException("Failed for search by keyword: " + userId, ex);
        }
    }

    /**
     * Filter cached enriched history by exact calendar date.
     */
    public List<EnrichedHistoryDto> searchByDate(UUID userId, LocalDate date) {
        try {
            return getEnrichedHistory(userId).stream()
                    .filter(dto -> dto.getViewedAt().toLocalDate().equals(date))
                    .collect(Collectors.toList());

        } catch (Exception ex) {
            throw new RuntimeException("Failed for search by date: " + userId, ex);
        }
    }
}
