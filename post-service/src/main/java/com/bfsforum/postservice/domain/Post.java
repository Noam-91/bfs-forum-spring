package com.bfsforum.postservice.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {
	@Id
	private String id;
	
	@NotNull
	private Long userId;
	
	@NotBlank
	private String title;
	
	@NotBlank
	private String content;
	
	private Boolean isArchived = false;
	
	private PostStatus status = PostStatus.UNPUBLISHED;  // post status
	
	private LocalDateTime createdAt = LocalDateTime.now();
	private LocalDateTime updatedAt = LocalDateTime.now();
	
	
	// attachments
	private List<String> images = new ArrayList<>();
	private List<String> attachments = new ArrayList<>();
	
	private List<PostReply> postReplies = new ArrayList<>();
	
	// 统计字段 (可以通过postReplies计算，但为了性能可以单独存储)
	private Integer viewCount = 0;
	private Integer replyCount = 0;
	
//	public void setTitle(String title) {
//		this.title = title;
//	}
}



