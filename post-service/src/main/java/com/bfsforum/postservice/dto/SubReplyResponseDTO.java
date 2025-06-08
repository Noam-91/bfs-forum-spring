package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-06-08
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubReplyResponseDTO {
	private String subReplyId;
	private String parentReplyId;
	private Long userId;
	private String comment;
	private Boolean isActive;
	private LocalDateTime dateCreated;
	
	// user info
	private UserInfoDTO userInfo;
}
