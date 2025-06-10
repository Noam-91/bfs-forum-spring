package com.bfsforum.historyservice.service;

import com.bfsforum.historyservice.domain.History;
import com.bfsforum.historyservice.domain.Post;
import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.dto.PostDto;
import com.bfsforum.historyservice.repository.HistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock HistoryRepo historyRepo;
    @Mock StreamBridge streamBridge;
    @Mock @Qualifier("postListManager")
    RequestReplyManager<List<Post>> requestReplyManager;

    @InjectMocks HistoryService historyService;

    private final String USER = "user-1";
    private final String POST_A = "post-A";
    private final LocalDateTime NOW = LocalDateTime.of(2025,6,9,12,0);


    @Test
    void recordView_updatesExisting() {
        LocalDateTime originalTime = LocalDateTime.of(2025,1,1,0,0);
        History existing = History.builder()
                .userId(USER)
                .postId(POST_A)
                .viewedAt(originalTime)
                .build();
        when(historyRepo.findByUserIdAndPostId(USER, POST_A))
                .thenReturn(Optional.of(existing));
        when(historyRepo.save(existing)).thenAnswer(inv -> inv.getArgument(0));

        History result = historyService.recordView(USER, POST_A);
        // compare to the original timestamp, not the mutated one
        assertThat(result.getViewedAt()).isAfter(originalTime);
        verify(historyRepo).save(existing);
    }

    @Test
    void recordView_createsNew_whenNoExisting() {
        when(historyRepo.findByUserIdAndPostId(USER, POST_A))
                .thenReturn(Optional.empty());
        ArgumentCaptor<History> captor = ArgumentCaptor.forClass(History.class);

        History saved = History.builder()
                .userId(USER)
                .postId(POST_A)
                .viewedAt(NOW)
                .build();
        when(historyRepo.save(any())).thenReturn(saved);

        History result = historyService.recordView(USER, POST_A);

        verify(historyRepo).save(captor.capture());
        History toSave = captor.getValue();
        assertThat(toSave.getUserId()).isEqualTo(USER);
        assertThat(toSave.getPostId()).isEqualTo(POST_A);
        assertThat(result).isSameAs(saved);
    }

    @Test
    void loadFullEnrichedHistory_returnsEmpty_whenNoHistory() {
        when(historyRepo.findByUserIdOrderByViewedAtDesc(USER))
                .thenReturn(Collections.emptyList());

        List<EnrichedHistoryDto> output = historyService.loadFullEnrichedHistory(USER);
        assertThat(output).isEmpty();
        verify(requestReplyManager, never()).awaitFuture(any(), any());
    }

    @Test
    void loadFullEnrichedHistory_mergesPostsCorrectly() throws Exception {
        // 1) stub history repo
        History h1 = History.builder().userId(USER).postId("p1")
                .viewedAt(LocalDateTime.of(2025,6,8,10,0)).build();
        History h2 = History.builder().userId(USER).postId("p2")
                .viewedAt(LocalDateTime.of(2025,6,9,11,0)).build();
        when(historyRepo.findByUserIdOrderByViewedAtDesc(USER))
                .thenReturn(List.of(h2, h1));

        // 2) stub requestReplyManager
        CompletableFuture<List<Post>> future = new CompletableFuture<>();
        when(requestReplyManager.createAndStoreFuture(any()))
                .thenReturn(future);
        List<Post> posts = List.of(
                Post.builder()
                        .id("p1")
                        .title("Title1")
                        .content("Body1")
                        .build(),
                Post.builder()
                        .id("p2")
                        .title("Title2")
                        .content("Body2")
                        .build()
        );
        // simulate async reply
        future.complete(posts);
        when(requestReplyManager.awaitFuture(any(), eq(future)))
                .thenReturn(posts);

        List<EnrichedHistoryDto> enriched = historyService.loadFullEnrichedHistory(USER);

        assertThat(enriched)
                .hasSize(2)
                .extracting(EnrichedHistoryDto::getPostId)
                .containsExactly("p2","p1");
        assertThat(enriched.get(0).getPost().getTitle()).isEqualTo("Title2");
    }


}
