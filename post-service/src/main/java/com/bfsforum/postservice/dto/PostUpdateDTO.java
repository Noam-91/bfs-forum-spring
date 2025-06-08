package com.bfsforum.postservice.dto;

import com.bfsforum.postservice.domain.PostStatus;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostUpdateDTO {
	
	@Size(min = 1, max = 200, message = "Title must be between 1 and 200 characters")
	private String title;
	
	@Size(min = 1, max = 10000, message = "Content must be between 1 and 10000 characters")
	private String content;
	
	private PostStatus status;
	
	private Boolean isArchived;
	
	// 可选的图片和附件更新
	private List<String> images;
	private List<String> attachments;
}