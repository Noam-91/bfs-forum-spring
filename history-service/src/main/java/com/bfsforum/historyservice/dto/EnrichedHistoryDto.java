package com.bfsforum.historyservice.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EnrichedHistoryDto {
    private String postId;
    private LocalDateTime viewedAt;
    private PostDto    post;
}
