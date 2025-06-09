package com.bfsforum.historyservice.controller;


import com.bfsforum.historyservice.domain.History;
import com.bfsforum.historyservice.domain.HistoryTest;
import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.service.HistoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import java.util.UUID;

@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<Page<EnrichedHistoryDto>> getHistory(
            @RequestHeader(name = "X-User-Id") UUID userId,
            @PageableDefault(page = 0, size = 3, sort = "viewedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<EnrichedHistoryDto> data = historyService.getEnrichedHistory(userId, pageable);
        return ResponseEntity.ok(data);

    }
    // test controller for saving a String in db
    @PutMapping("/save")
    public ResponseEntity<?> saveHistory(
            @RequestParam("userId") String userId,
            @RequestParam("postId") String postId
    ){
        System.out.println("History save test start:");
        HistoryTest h = historyService.recordViewInString(userId, postId);
        return ResponseEntity.ok(h);
    }

    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(page = 0, size = 3, sort = "viewedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        if (keyword != null && date != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please supply either keyword or date, not both."
            );
            }
            Page<EnrichedHistoryDto> page;
            if (keyword != null) {
                page = historyService.searchByKeyword(userId, keyword, pageable);
            } else if (date != null) {
                page = historyService.searchByDate(userId, date, pageable);
            } else {
                page = historyService.getEnrichedHistory(userId, pageable);
            }

            return ResponseEntity.ok(page);

    }
}