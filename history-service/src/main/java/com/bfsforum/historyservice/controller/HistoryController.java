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
import java.util.Optional;

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
    @GetMapping("/search")
    @Operation(
            summary = "Search viewed history",
            description = "Search viewed history by keyword or by a start–end date range for the authenticated user"
                    + "If keyword is provided, date parameters are ignored. "
                    + "If dates are provided, missing startDate defaults to 1900-01-01 and missing endDate defaults to today. "
                    + "startDate must be on or before endDate. "
                    + "If neither keyword nor dates are provided, returns full history."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search results retrieved", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "Bad request if both keyword and date-range are provided, or startDate after endDate is supplied", content = @Content),
            @ApiResponse(responseCode = "504", description = "Timeout waiting for post-service response during search", content = @Content),
            @ApiResponse(responseCode = "500", description = "Internal server error", content = @Content)})

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
//        // 2. If user wants a date range, both bounds must be present
//        if ((startDate != null && endDate == null) || (startDate == null && endDate != null)) {
//            throw new ResponseStatusException(
//                    HttpStatus.BAD_REQUEST,
//                    "Please supply both startDate and endDate for a date range search."
//            );
//        }
        Page<EnrichedHistoryDto> page;
        if (keyword != null) {
            // keyword search
            page = historyService.searchByKeyword(userId, keyword, pageable);

        } else {
            // no param cases:
            // default start date set to be 01/01/1900
            LocalDate lowerBound = Optional.ofNullable(startDate).
                    orElse(LocalDate.of(1900, 1, 1));
            // default end date set to be today
            LocalDate upperBound = Optional.ofNullable(endDate).orElse(LocalDate.now());

            // check if start date is later than end date
            if(lowerBound.isAfter(upperBound)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "startDate must be on or after endDate."
                );
            }

            // if neither date provided, gives full history
                if(startDate == null && endDate == null) {
                    page = historyService.getEnrichedHistory(userId, pageable);
                } else {
                    page = historyService.searchByDate(userId, lowerBound, upperBound, pageable);
                }

        }

        return ResponseEntity.ok(page);
    }
}