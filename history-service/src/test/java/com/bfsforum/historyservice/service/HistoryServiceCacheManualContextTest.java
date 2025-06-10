//package com.bfsforum.historyservice.service;
//import com.bfsforum.historyservice.domain.History;
//import com.bfsforum.historyservice.domain.Post;
//import com.bfsforum.historyservice.domain.PostStatus;
//import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
//import com.bfsforum.historyservice.repository.HistoryRepo;
//import com.bfsforum.historyservice.service.HistoryService;
//import com.bfsforum.historyservice.service.RequestReplyManager;
//import org.junit.jupiter.api.Test;
//import org.springframework.cache.CacheManager;
//import org.springframework.cache.annotation.EnableCaching;
//import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
//import org.springframework.cloud.stream.function.StreamBridge;
//import org.springframework.context.annotation.AnnotationConfigApplicationContext;
//import org.springframework.context.annotation.Configuration;
//
//import java.time.LocalDateTime;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//public class HistoryServiceCacheManualContextTest {
//
//    @Configuration
//    @EnableCaching
//    static class CacheConfig {}
//
//    @Test
//    void loadFullEnrichedHistory_shouldBeCached() {
//        // 1) Build a minimal Spring context
//        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
//
//        // 2) Enable @Cacheable processing
//        ctx.register(CacheConfig.class);
//
//        // 3) Provide an in-memory CacheManager for "history"
//        ctx.registerBean(CacheManager.class,
//                () -> new ConcurrentMapCacheManager("history"));
//
//        // 4) Create & register Mockito mocks for dependencies
//        HistoryRepo historyRepo = mock(HistoryRepo.class);
//        StreamBridge streamBridge = mock(StreamBridge.class);
//        @SuppressWarnings("unchecked")
//        RequestReplyManager<List<Post>> requestReplyManager = mock(RequestReplyManager.class);
//
//        ctx.registerBean(HistoryRepo.class,         () -> historyRepo);
//        ctx.registerBean(StreamBridge.class,        () -> streamBridge);
//        ctx.registerBean(RequestReplyManager.class, () -> requestReplyManager);
//
//        // 5) Register the service bean (Spring will wrap it for caching)
//        ctx.registerBean(HistoryService.class, () ->
//                new HistoryService(historyRepo, streamBridge, requestReplyManager)
//        );
//
//        // 6) Refresh and retrieve the proxied service
//        ctx.refresh();
//        HistoryService svc = ctx.getBean(HistoryService.class);
//
//        // 7) Stub repository to return a single History record
//        History h = History.builder()
//                .userId("user-1")
//                .postId("p1")
//                .viewedAt(LocalDateTime.now())
//                .build();
//        when(historyRepo.findByUserIdOrderByViewedAtDesc("user-1"))
//                .thenReturn(List.of(h));
//
//        // 8) Stub reply manager to return a completed future of Posts
//        List<Post> posts = List.of(
//                Post.builder().id("p1")
//                .userId("test123")
//                .firstName("Harry")
//                .lastName("Potter")
//                .title("Test Title")
//                .content("Test Content")
//                .isArchived(false)
//                .status(PostStatus.PUBLISHED)
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .images(List.of())
//                .attachments(List.of())
//                .postReplies(List.of())
//                .viewCount(0)
//                .replyCount(0)
//                .build()
//        );
//        CompletableFuture<List<Post>> fut = CompletableFuture.completedFuture(posts);
//        when(requestReplyManager.createAndStoreFuture(anyString())).thenReturn(fut);
//        when(requestReplyManager.awaitFuture(anyString(), any())).thenReturn(posts);
//
//        // 9) FIRST invocation → should hit repo & manager
//        List<EnrichedHistoryDto> first = svc.loadFullEnrichedHistory("user-1");
//
//// verify repository was called once
//        verify(historyRepo, times(1))
//                .findByUserIdOrderByViewedAtDesc("user-1");
//
//// verify both manager calls
//        verify(requestReplyManager, times(1))
//                .createAndStoreFuture(anyString());
//        verify(requestReplyManager, times(1))
//                .awaitFuture(anyString(), any());
//
//// now second call …
//        List<EnrichedHistoryDto> second = svc.loadFullEnrichedHistory("user-1");
//
//// now assert no new interactions on *either* mock
//        verifyNoMoreInteractions(historyRepo, requestReplyManager);
//
//        ctx.close();
//    }
//}