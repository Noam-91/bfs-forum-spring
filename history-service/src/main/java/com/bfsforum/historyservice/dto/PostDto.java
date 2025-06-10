package com.bfsforum.historyservice.dto;

import lombok.*;

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
    // to be discussed
}
