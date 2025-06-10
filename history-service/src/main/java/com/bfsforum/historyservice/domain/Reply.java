package com.bfsforum.historyservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-06
 */

// replies nested in posts
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reply {
    @Builder.Default
    private String id = UUID.randomUUID().toString();           // automatically generated
    private String userId;
    private String comment;

    @Builder.Default
    private Boolean isActive = true;  // isActive (false -> deleted)

    private LocalDateTime createdAt;

    @Builder.Default
    private List<SubReply> subReplies = new ArrayList<>();
}
