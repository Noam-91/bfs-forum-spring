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
	

	// Getters and setters

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getIsArchived() {
		return isArchived;
	}

	public void setIsArchived(Boolean archived) {
		isArchived = archived;
	}

	public PostStatus getStatus() {
		return status;
	}

	public void setStatus(PostStatus status) {
		this.status = status;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public List<String> getAttachments() {
		return attachments;
	}

	public void setAttachments(List<String> attachments) {
		this.attachments = attachments;
	}

	public List<PostReply> getPostReplies() {
		return postReplies;
	}

	public void setPostReplies(List<PostReply> postReplies) {
		this.postReplies = postReplies;
	}

	public Integer getViewCount() {
		return viewCount;
	}

	public void setViewCount(Integer viewCount) {
		this.viewCount = viewCount;
	}

	public Integer getReplyCount() {
		return replyCount;
	}

	public void setReplyCount(Integer replyCount) {
		this.replyCount = replyCount;
	}
}



