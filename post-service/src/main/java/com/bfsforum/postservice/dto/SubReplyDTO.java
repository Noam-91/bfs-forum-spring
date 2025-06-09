package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubReplyDTO {
	private Long userId;
	private String comment;
}
