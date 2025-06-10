package com.bfsforum.historyservice.domain;

import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
    private String id;

    private String userId;

    private String title;


    private String content;

    private Boolean isArchived = false;

    private PostStatus status = PostStatus.UNPUBLISHED;  // post status

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();


    private List<String> images = new ArrayList<>();
    private List<String> attachments = new ArrayList<>();

    private List<PostReply> postReplies = new ArrayList<>();

    private Integer viewCount = 0;
    private Integer replyCount = 0;

}
