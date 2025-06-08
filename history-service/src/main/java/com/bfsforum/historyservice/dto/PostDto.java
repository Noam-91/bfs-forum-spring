package com.bfsforum.historyservice.dto;

import lombok.*;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private String postId;
    private String  title;
    private String  content;
    // to be discussed
}
