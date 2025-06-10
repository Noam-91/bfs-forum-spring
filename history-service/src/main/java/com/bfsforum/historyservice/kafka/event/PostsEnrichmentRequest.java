package com.bfsforum.historyservice.kafka.event;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostsEnrichmentRequest {
    private String         requestId;
    private List<String> postIds;
}
