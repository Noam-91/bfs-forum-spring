package com.bfsforum.historyservice.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post {
	@Id
	private String id;

	private String userId;

	private String firstName;

	private String lastName;

	private String title;

	private String content;

	@Builder.Default
	private String status = PostStatus.UNPUBLISHED.toString();
	
	private LocalDateTime createdAt;

	private LocalDateTime updatedAt;

	// attachments
	@Builder.Default
	private List<String> images = new ArrayList<>();
	@Builder.Default
	private List<String> attachments = new ArrayList<>();
	@Builder.Default
	private List<Reply> replies = new ArrayList<>();
	@Builder.Default
	private Integer viewCount = 0;
	@Builder.Default
	private Integer replyCount = 0;

}



