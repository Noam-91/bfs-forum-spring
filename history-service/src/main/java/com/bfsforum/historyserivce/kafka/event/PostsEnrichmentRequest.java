package com.bfsforum.historyserivce.kafka.event;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostsEnrichmentRequest {
    private UUID          requestId;
    private List<UUID> postIds;
}
