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
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.data.domain.*;
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

//    @Test
//    void getEnrichedHistory_appliesPaging() {
//        // spy so we can stub loadFullEnrichedHistory
//        HistoryService spySvc = Mockito.spy(historyService);
//        List<EnrichedHistoryDto> list = new ArrayList<>();
//        for (int i = 0; i < 5; i++) {
//            list.add(EnrichedHistoryDto.builder()
//                    .postId("p"+i)
//                    .viewedAt(LocalDateTime.now())
//                    .post(PostDto.builder().postId("p"+i).title("T").content("C").build())
//                    .build());
//        }
//        doReturn(list).when(spySvc).loadFullEnrichedHistory(USER);
//
//        Pageable page2of3 = PageRequest.of(1, 3);
//        Page<EnrichedHistoryDto> page = spySvc.getEnrichedHistory(USER, page2of3);
//
//        assertThat(page.getTotalElements()).isEqualTo(5);
//        assertThat(page.getContent()).hasSize(2);
//        assertThat(page.getContent().get(0).getPostId()).isEqualTo("p3");
//    }
@Test
void getEnrichedHistory_appliesPaging() {
    // 1) create a spy on your real HistoryService
    HistoryService spySvc = Mockito.spy(historyService);

    // 2) inject the spy into its own 'self' field so getEnrichedHistory()
    //    will call spySvc.loadFullEnrichedHistory(...)
    ReflectionTestUtils.setField(spySvc, "self", spySvc);

    // 3) prepare a list of 5 items and stub loadFullEnrichedHistory
    List<EnrichedHistoryDto> list = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        list.add(EnrichedHistoryDto.builder()
                .postId("p" + i)
                .viewedAt(LocalDateTime.now())
                .post(PostDto.builder().postId("p" + i).title("T").content("C").build())
                .build());
    }
    doReturn(list).when(spySvc).loadFullEnrichedHistory(USER);

    // 4) exercise paging
    Pageable page2of3 = PageRequest.of(1, 3);
    Page<EnrichedHistoryDto> page = spySvc.getEnrichedHistory(USER, page2of3);

    // 5) assert
    assertThat(page.getTotalElements()).isEqualTo(5);
    assertThat(page.getContent()).hasSize(2);
    assertThat(page.getContent().get(0).getPostId()).isEqualTo("p3");
}

//    @Test
//    void searchByDate_filtersCorrectly() {
//        HistoryService spySvc = Mockito.spy(historyService);
//        LocalDate d1 = LocalDate.of(2025,6,1);
//        LocalDate d2 = LocalDate.of(2025,6,5);
//        // one inside, one before, one after
//        List<EnrichedHistoryDto> list = List.of(
//                EnrichedHistoryDto.builder()
//                        .postId("a")
//                        .viewedAt(d1.atStartOfDay())
//                        .post(null).build(),
//                EnrichedHistoryDto.builder()
//                        .postId("b")
//                        .viewedAt(LocalDate.of(2025,5,30).atStartOfDay())
//                        .post(null).build(),
//                EnrichedHistoryDto.builder()
//                        .postId("c")
//                        .viewedAt(LocalDate.of(2025,6,10).atStartOfDay())
//                        .post(null).build()
//        );
//        doReturn(list).when(spySvc).loadFullEnrichedHistory(USER);
//
//        Pageable all = PageRequest.of(0, 10);
//        Page<EnrichedHistoryDto> result = spySvc.searchByDate(USER, d1, d2, all);
//
//        assertThat(result.getContent())
//                .extracting(EnrichedHistoryDto::getPostId)
//                .containsExactly("a");
//    }
@Test
void searchByDate_filtersCorrectly() {
    // 1) spy the real service
    HistoryService spySvc = Mockito.spy(historyService);
    // 2) wire the spy back into its own `self` field
    ReflectionTestUtils.setField(spySvc, "self", spySvc);

    // 3) define date range and sample data
    LocalDate d1 = LocalDate.of(2025, 6, 1);
    LocalDate d2 = LocalDate.of(2025, 6, 5);
    List<EnrichedHistoryDto> list = List.of(
            EnrichedHistoryDto.builder()
                    .postId("a")
                    .viewedAt(d1.atStartOfDay())      // inside range
                    .post(null)
                    .build(),
            EnrichedHistoryDto.builder()
                    .postId("b")
                    .viewedAt(LocalDate.of(2025, 5, 30).atStartOfDay())  // before
                    .post(null)
                    .build(),
            EnrichedHistoryDto.builder()
                    .postId("c")
                    .viewedAt(LocalDate.of(2025, 6, 10).atStartOfDay())  // after
                    .post(null)
                    .build()
    );
    // 4) stub the enrichment call
    doReturn(list).when(spySvc).loadFullEnrichedHistory(USER);

    // 5) exercise the filter
    Pageable all = PageRequest.of(0, 10);
    Page<EnrichedHistoryDto> result = spySvc.searchByDate(USER, d1, d2, all);

    // 6) verify only the in‐range item remains
    assertThat(result.getContent())
            .extracting(EnrichedHistoryDto::getPostId)
            .containsExactly("a");
}
}
