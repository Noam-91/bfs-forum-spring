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
public class PostDto {
    private String postId;
    private String  title;
    private String  content;
    private String  firstName;
    private String  lastName;
    private Integer viewCount;
    private Integer replyCount;
}
