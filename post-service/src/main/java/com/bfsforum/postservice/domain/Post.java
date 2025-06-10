package com.bfsforum.postservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-06
 */

@Document(collection = "posts")
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

	@NotBlank(message = "Title cannot be blank")
	@Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
	private String title;

	@NotBlank(message = "Content cannot be blank")
	@Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
	private String content;

	@Builder.Default
	private String status = PostStatus.UNPUBLISHED.toString();

	@Builder.Default
	private Boolean isArchived = false;	//todo: archived related
	
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



