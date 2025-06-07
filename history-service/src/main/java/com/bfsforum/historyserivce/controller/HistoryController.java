package com.bfsforum.historyserivce.controller;

import com.bfsforum.historyserivce.dto.EnrichedHistoryDto;
import com.bfsforum.historyserivce.dto.common.DataResponse;
import com.bfsforum.historyserivce.service.HistoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<DataResponse> getHistory(
            @RequestHeader(name = "X-User-Id") UUID userId
    ) {

            List<EnrichedHistoryDto> data = historyService.getEnrichedHistory(userId);
            return ResponseEntity.ok(DataResponse.builder().message("History fetch success").data(data).build());

    }

    @GetMapping("/search")
    public ResponseEntity<DataResponse> search(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        if (keyword != null && date != null) {
            return ResponseEntity
                    .badRequest()
                    .body(DataResponse.builder()
                            .message("Please supply either keyword or date, not at the same time.")
                            .data(null)
                            .build());
        }

        if(keyword != null) {
            List<EnrichedHistoryDto> keywordData = historyService.searchByKeyword(userId, keyword);
            return ResponseEntity.ok(
                    DataResponse.builder()
                            .message("Filtered by keyword")
                            .data(keywordData)
                            .build()
            );
        }
        if(date != null) {
            List<EnrichedHistoryDto> dateData = historyService.searchByDate(userId, date);
            return ResponseEntity.ok(
                    DataResponse.builder()
                            .message("Filtered by date")
                            .data(dateData)
                            .build());
        }
        // if both params are null
        List<EnrichedHistoryDto> all = historyService.getEnrichedHistory(userId);
        return ResponseEntity.ok(DataResponse.builder()
                .message("Full history")
                .data(all)
                .build());
    }



}
