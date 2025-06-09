package com.bfsforum.historyserivce.service;

import com.bfsforum.historyserivce.domain.History;
import com.bfsforum.historyserivce.repository.HistoryRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class HistoryServiceTest {

    @Mock
    private HistoryRepo repo;

    @InjectMocks
    private HistoryService service;

    private UUID userId;
    private UUID postId;
    private History example;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        postId = UUID.randomUUID();
        now = LocalDateTime.now();
        example = new History(userId, postId, now.minusDays(1));
    }

    @Test
    void getByUserAndPost_found() {
        when(repo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(example));

        Optional<History> result = service.getByUserAndPost(userId, postId);

        assertThat(result).isPresent()
                .contains(example);
        verify(repo).findByUserIdAndPostId(userId, postId);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void getByUserAndPost_notFound() {
        when(repo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());

        Optional<History> result = service.getByUserAndPost(userId, postId);

        assertThat(result).isEmpty();
        verify(repo).findByUserIdAndPostId(userId, postId);
        verifyNoMoreInteractions(repo);
    }


    @Test
    void recordView_existingHistory_updatesTimestamp() {
        History existing = new History(UUID.randomUUID(), userId, postId, now.minusDays(5));

        when(repo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.of(existing));
        when(repo.save(existing)).thenReturn(existing);

        History result = service.recordView(userId, postId, now);

        assertSame(existing, result);
        assertEquals(now, existing.getViewedAt());
        verify(repo).findByUserIdAndPostId(userId, postId);
        verify(repo).save(existing);
        verifyNoMoreInteractions(repo);
    }

    @Test
    void recordView_noHistory_createsAndSavesNew() {
        when(repo.findByUserIdAndPostId(userId, postId))
                .thenReturn(Optional.empty());
        when(repo.save(any(History.class))).thenAnswer(invocation -> {
            History h = invocation.getArgument(0);
            h.setHistoryId(UUID.randomUUID());   // <— assign ID
            return h;
        });

        History result = service.recordView(userId, postId, now);

        assertNotNull(result.getHistoryId());
        assertEquals(userId, result.getUserId());
        assertEquals(postId, result.getPostId());
        assertEquals(now, result.getViewedAt());
        verify(repo).findByUserIdAndPostId(userId, postId);
        verify(repo).save(any(History.class));
        verifyNoMoreInteractions(repo);
    }
}