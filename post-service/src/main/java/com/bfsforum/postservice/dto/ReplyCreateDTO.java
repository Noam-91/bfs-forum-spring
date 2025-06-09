package com.bfsforum.postservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplyCreateDTO {
	@NotNull(message = "User ID cannot be null")
	private Long userId;
	
	@NotBlank(message = "Comment cannot be blank")
	@Size(min = 1, max = 1000, message = "Comment must be between 1 and 1000 characters")
	private String comment;
	
	// if it's a subreply, parentReplyId is needed
	private String parentReplyId;
}
