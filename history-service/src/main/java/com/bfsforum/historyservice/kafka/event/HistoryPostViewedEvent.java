package com.bfsforum.historyservice.kafka.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistoryPostViewedEvent {
    private String userId;
    private String postId;
    private LocalDateTime viewedAt;
}
