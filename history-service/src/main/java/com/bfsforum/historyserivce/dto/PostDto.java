package com.bfsforum.historyserivce.dto;

import lombok.*;

import java.util.UUID;

@Data
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {
    private UUID postId;
    private String  title;
    private String  content;
    // to be discussed
}
