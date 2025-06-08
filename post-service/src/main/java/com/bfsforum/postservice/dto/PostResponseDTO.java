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
public class PostResponseDTO {
	private String id;
	private Long userId;
	private String title;
	private String content;
	private PostStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
	
	private Integer viewCount;
	private Integer replyCount;
	
	private List<String> iamges;
	private List<String> attachments;
	
	// reply to messages
	private List<ReplyResponseDTO> replies;
	
	// userInfo from other services
	private UserInfoDTO userInfo;
}
