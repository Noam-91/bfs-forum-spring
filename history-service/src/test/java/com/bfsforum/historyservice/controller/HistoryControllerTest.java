
package com.bfsforum.historyservice.controller;
import com.bfsforum.historyservice.controller.HistoryController;
import com.bfsforum.historyservice.controller.HistoryController;
import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.dto.PostDto;
import com.bfsforum.historyservice.exception.GlobalExceptionHandler;
import com.bfsforum.historyservice.service.HistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.data.domain.*;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import static org.junit.jupiter.api.Assertions.*;
@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        // stub default behavior for full history with empty page
        Page<EnrichedHistoryDto> emptyPage = Page.empty();
        when(historyService.getEnrichedHistory(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(historyService.searchByDate(anyString(), any(LocalDate.class), any(LocalDate.class), any(Pageable.class)))
                .thenReturn(emptyPage);
        when(historyService.searchByKeyword(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);
    }


    private EnrichedHistoryDto sampleDto() {
        String postId = UUID.randomUUID().toString();
        LocalDateTime viewedAt = LocalDateTime.now();
        PostDto post = PostDto.builder()
                .postId("p1")
                .title("Test Title")
                .content("Test Content")
                .firstName("Harry")
                .lastName("Potter")
                .viewCount(123)
                .replyCount(45)
                .build();
        return new EnrichedHistoryDto(postId, viewedAt, post);
    }


    @Test
    void getHistory_returnsPage() throws Exception {
        String userId = "user-123";
        EnrichedHistoryDto dto = sampleDto();
        Page<EnrichedHistoryDto> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 3, Sort.by("viewedAt").descending()),
                1
        );

        given(historyService.getEnrichedHistory(eq(userId), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/history")
                        .header("X-User-Id", userId)
                        .param("page", "0")
                        .param("size", "3")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].postId").value(dto.getPostId()));
    }

    @Test
    void search_byKeyword_returnsPage() throws Exception {
        String userId = "user-456";
        String keyword = "foo";
        EnrichedHistoryDto dto = sampleDto();
        Page<EnrichedHistoryDto> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 3, Sort.by("viewedAt").descending()),
                1
        );

        given(historyService.searchByKeyword(eq(userId), eq(keyword), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", userId)
                        .param("keyword", keyword)
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void search_byDateRange_returnsPage() throws Exception {
        String userId = "user-789";
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end = LocalDate.of(2025, 6, 5);
        EnrichedHistoryDto dto = sampleDto();
        Page<EnrichedHistoryDto> page = new PageImpl<>(
                List.of(dto),
                PageRequest.of(0, 3, Sort.by("viewedAt").descending()),
                1
        );

        given(historyService.searchByDate(eq(userId), eq(start), eq(end), any(Pageable.class)))
                .willReturn(page);

        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", userId)
                        .param("date", start.toString())
                        .param("endDate", end.toString())
                        .param("page", "0")
                        .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void search_invalidBothKeywordAndDate_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", "u1")
                        .param("keyword", "foo")
                        .param("date", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().toString()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void search_noParams_returnsFullHistory() throws Exception {
        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", "u2"))
                .andExpect(status().isOk());
        verify(historyService).getEnrichedHistory(eq("u2"), any(Pageable.class));
    }
    @Test
    void search_keywordOnly_invokesKeywordSearch() throws Exception {
        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", "u2")
                        .param("keyword", "foo"))
                .andExpect(status().isOk());
        verify(historyService).searchByKeyword(eq("u2"), eq("foo"), any(Pageable.class));
    }

    @Test
    void search_dateOnly_defaultsEndDateAndOk() throws Exception {
        String start = LocalDate.of(2025, 6, 1).toString();

        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", "u2")
                        .param("date", start)             // ← use "date"
                        .param("endDate", (String) null)  // make sure endDate is absent
                )
                .andExpect(status().isOk());

        // lowerBound = start, upperBound = today
        verify(historyService).searchByDate(
                eq("u2"),
                eq(LocalDate.parse(start)),
                eq(LocalDate.now()),
                any(Pageable.class)
        );
    }

    @Test
    void search_endDateOnly_defaultsStartDateAndOk() throws Exception {
        String end = LocalDate.of(2025, 6, 5).toString();
        mockMvc.perform(get("/history/search")
                        .header("X-User-Id", "u2")
                        .param("endDate", end))
                .andExpect(status().isOk());
        // lowerBound=1900-01-01, upperBound=end
        verify(historyService).searchByDate(
                eq("u2"),
                eq(LocalDate.of(1900, 1, 1)),
                eq(LocalDate.parse(end)),
                any(Pageable.class)
        );
    }


}