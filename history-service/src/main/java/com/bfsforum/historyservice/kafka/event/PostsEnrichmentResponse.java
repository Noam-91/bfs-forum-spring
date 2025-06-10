package com.bfsforum.historyservice.kafka.event;
import com.bfsforum.historyservice.dto.PostDto;
import lombok.*;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostsEnrichmentResponse {
//    private String requestId;
    private List<PostDto> posts;

  public <E> PostsEnrichmentResponse(String string, List<E> pd) {
      this.posts = (List<PostDto>) pd;
  }
}
