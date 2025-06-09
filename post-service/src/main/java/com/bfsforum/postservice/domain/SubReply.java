package com.bfsforum.postservice.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * @author luluxue
 * @date 2025-06-09
 */

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubReply {
	private Long userId;
	private String comment;
	private Boolean isActive = true;
	private LocalDateTime createdAt;
}
