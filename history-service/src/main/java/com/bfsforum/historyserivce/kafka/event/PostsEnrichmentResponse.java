package com.bfsforum.historyserivce.kafka.event;
import com.bfsforum.historyserivce.dto.PostDto;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostsEnrichmentResponse {
    private UUID requestId;
    private List<PostDto> posts;
}
