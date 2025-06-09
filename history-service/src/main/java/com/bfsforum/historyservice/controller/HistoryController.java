package com.bfsforum.historyservice.controller;


import com.bfsforum.historyservice.dto.EnrichedHistoryDto;
import com.bfsforum.historyservice.service.HistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "History", description = "Operations related to user history retrieval and management")
@RestController
@RequestMapping("/history")
public class HistoryController {
    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    @Operation(summary = "Get paginated viewed history", description = "Retrieve paginated viewed history entries for the authenticated user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "History retrieved successfully", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "504", description = "Timeout waiting for post-service response", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    public ResponseEntity<Page<EnrichedHistoryDto>> getHistory(
            @RequestHeader(name = "X-User-Id") String userId,
            @PageableDefault(page = 0, size = 3, sort = "viewedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {

        Page<EnrichedHistoryDto> data = historyService.getEnrichedHistory(userId, pageable);
        return ResponseEntity.ok(data);

    }
    //    // test controller for saving a String in db
//    @PutMapping("/save")
//    public ResponseEntity<?> saveHistory(
//            @RequestParam("userId") String userId,
//            @RequestParam("postId") String postId
//    ){
//        System.out.println("History save test start:");
//        HistoryTest h = historyService.recordViewInString(userId, postId);
//        return ResponseEntity.ok(h);
//    }
    @Operation(
            summary = "Search viewed history",
            description = "Search viewed history by keyword or by a start–end date range for the authenticated user"
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request if both keyword and date-range are provided, or if only one of startDate/endDate is supplied", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout waiting for post-service response during search", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})
    @GetMapping("/search")
    public ResponseEntity<?> search(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(name = "date", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(name = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @PageableDefault(page = 0, size = 3, sort = "viewedAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        // 1. Don’t allow mixing keyword and date-range
        if (keyword != null && (startDate != null || endDate != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please supply either a keyword or a date range, not both."
            );
        }
        // 2. If user wants a date range, both bounds must be present
        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please supply both startDate and endDate for a date range search."
            );
        }
        Page<EnrichedHistoryDto> page;
        if (keyword != null) {
            page = historyService.searchByKeyword(userId, keyword, pageable);

        } else if (startDate != null) {
            // both startDate and endDate are not null here
            page = historyService.searchByDate(userId, startDate, endDate, pageable);

        } else {
            // neither keyword nor dates supplied gives full history
            page = historyService.getEnrichedHistory(userId, pageable);
        }

        return ResponseEntity.ok(page);
    }
}