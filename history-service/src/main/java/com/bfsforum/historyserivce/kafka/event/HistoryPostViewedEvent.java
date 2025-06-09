package com.bfsforum.historyserivce.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryPostViewedEvent {
    private UUID userId;
    private UUID postId;
    private LocalDateTime viewedAt;
}
