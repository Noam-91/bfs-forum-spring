package com.bfsforum.postservice.dto;

import com.bfsforum.postservice.domain.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO {
	
	private String id;
	private Long userId;
	private String title;
	private String content;
	private PostStatus status;
	private Boolean isArchrived;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	private Integer viewCount;
	private Integer replyCount;
	
	private List<String> iamges;
	private List<String> attachments;
	
	// 回复信息（可选，根据需要包含）
	private List<ReplyResponseDTO> replies;
	
	// 用户信息（从其他服务获取，可选）
	private UserInfoDTO userInfo;
}
