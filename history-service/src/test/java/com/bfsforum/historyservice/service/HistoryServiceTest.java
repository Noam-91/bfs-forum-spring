//package com.bfsforum.historyservice.service;
//
//import com.bfsforum.historyservice.domain.History;
//import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
//import com.bfsforum.historyservice.dto.PostDto;
//import com.bfsforum.historyservice.kafka.event.PostsEnrichmentRequest;
//import com.bfsforum.historyservice.kafka.event.PostsEnrichmentResponse;
//import com.bfsforum.historyservice.repository.HistoryRepo;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentMatchers;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.Mockito;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
//import org.springframework.kafka.requestreply.RequestReplyFuture;
//import org.springframework.util.concurrent.SettableListenableFuture;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.Optional;
//import java.util.UUID;
//import java.util.concurrent.TimeUnit;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.Mockito.*;
//
//
//@ExtendWith(MockitoExtension.class)
//class HistoryServiceTest {
//
//    @Mock
//    private HistoryRepo repo;
//    @Mock
//    ReplyingKafkaTemplate<String, PostsEnrichmentRequest, PostsEnrichmentResponse> kafka;
//
//
//    @InjectMocks
//    private HistoryService service;
//
//    private UUID userId;
//    private UUID postId;
//    private History example;
//    private LocalDateTime now;
//
//    @BeforeEach
//    void setUp() {
//        userId = UUID.randomUUID().toString();
//        postId = UUID.randomUUID().toString();
//        now = LocalDateTime.now();
//        example = new History(userId, postId, now.minusDays(1));
//    }
//
//    @Test
//    void getByUserAndPost_found() {
//        when(repo.findByUserIdAndPostId(userId, postId))
//                .thenReturn(Optional.of(example));
//
//        Optional<History> result = service.getByUserAndPost(userId, postId);
//
//        assertThat(result).isPresent()
//                .contains(example);
//        verify(repo).findByUserIdAndPostId(userId, postId);
//        verifyNoMoreInteractions(repo);
//    }
//
//    @Test
//    void getByUserAndPost_notFound() {
//        when(repo.findByUserIdAndPostId(userId, postId))
//                .thenReturn(Optional.empty());
//
//        Optional<History> result = service.getByUserAndPost(userId, postId);
//
//        assertThat(result).isEmpty();
//        verify(repo).findByUserIdAndPostId(userId, postId);
//        verifyNoMoreInteractions(repo);
//    }
//
//
//    @Test
//    void recordView_existingHistory_updatesTimestamp() {
//        History existing = new History(UUID.randomUUID().toString(), userId, postId, now.minusDays(5));
//
//        when(repo.findByUserIdAndPostId(userId, postId))
//                .thenReturn(Optional.of(existing));
//        when(repo.save(existing)).thenReturn(existing);
//
//        History result = service.recordView(userId, postId, now);
//
//        assertSame(existing, result);
//        assertEquals(now, existing.getViewedAt());
//        verify(repo).findByUserIdAndPostId(userId, postId);
//        verify(repo).save(existing);
//        verifyNoMoreInteractions(repo);
//    }
//
//    @Test
//    void recordView_noHistory_createsAndSavesNew() {
//        when(repo.findByUserIdAndPostId(userId, postId))
//                .thenReturn(Optional.empty());
//        when(repo.save(any(History.class))).thenAnswer(invocation -> {
//            History h = invocation.getArgument(0);
//            h.setHistoryId(UUID.randomUUID().toString());   // <— assign ID
//            return h;
//        });
//
//        History result = service.recordView(userId, postId, now);
//
//        assertNotNull(result.getHistoryId());
//        assertEquals(userId, result.getUserId());
//        assertEquals(postId, result.getPostId());
//        assertEquals(now, result.getViewedAt());
//        verify(repo).findByUserIdAndPostId(userId, postId);
//        verify(repo).save(any(History.class));
//        verifyNoMoreInteractions(repo);
//    }
//    @Test
//    void getEnrichedHistory_shouldReturnEnrichedAndBeCached() throws Exception {
//        // --- arrange ----------------------------------------------------
//        String userId = "user-123";
//        String postId1 = "post-A";
//        LocalDateTime ts1 = LocalDateTime.of(2025, 6, 7, 12, 0);
//
//        // stub repository
//        History h1 = new History(userId, postId1, ts1);
//        when(repo.findByUserIdOrderByViewedAtDesc(userId))
//                .thenReturn(List.of(h1));
//
//        // prepare fake enrichment response
//        PostDto pd = new PostDto(postId1, "Title A", "Body A");
//        PostsEnrichmentResponse response =
//                new PostsEnrichmentResponse(UUID.randomUUID().toString(), List.of(pd));
//        ConsumerRecord<String, PostsEnrichmentResponse> cr =
//                new ConsumerRecord<>("posts-enrichment-request", 0, 0L, null, response);
//
//        @SuppressWarnings("unchecked")
//        RequestReplyFuture<String, PostsEnrichmentRequest, PostsEnrichmentResponse> rrFuture =
//                mock(RequestReplyFuture.class);
//
//        when(rrFuture.get(5, TimeUnit.SECONDS)).thenReturn(cr);
//
//        // <<< DISAMBIGUATE to the ProducerRecord overload >>>
//        when(kafka.sendAndReceive(
//                ArgumentMatchers.<ProducerRecord<String, PostsEnrichmentRequest>>any()
//        )).thenReturn(rrFuture);
//
//        // --- act & assert first call ------------------------------------
//        List<EnrichedHistoryDto> first = service.loadFullEnrichedHistory(userId);
//        assertThat(first).hasSize(1);
//        assertThat(first.get(0).getPost()).isEqualTo(pd);
//
//        verify(repo, times(1)).findByUserIdOrderByViewedAtDesc(userId);
//        verify(kafka, times(1)).sendAndReceive(
//                ArgumentMatchers.<ProducerRecord<String, PostsEnrichmentRequest>>any()
//        );
//
//        // --- act & assert second call (cached) -------------------------
//        List<EnrichedHistoryDto> second = service.loadFullEnrichedHistory(userId);
//        assertThat(second).isEqualTo(first);  // identical instance
//
//        // still only one repo + kafka call in total
//        verify(repo, times(1)).findByUserIdOrderByViewedAtDesc(userId);
//        verify(kafka, times(1)).sendAndReceive(
//                ArgumentMatchers.<ProducerRecord<String, PostsEnrichmentRequest>>any()
//        );
//    }
//}