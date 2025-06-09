package com.bfsforum.historyserivce.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;
@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedHistoryDto {
    private UUID postId;
    private LocalDateTime viewedAt;
    private PostDto    post;
}
