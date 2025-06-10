package com.bfsforum.historyservice.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrichedHistoryDto {
    private String postId;
    private LocalDateTime viewedAt;
    private PostDto    post;
}
