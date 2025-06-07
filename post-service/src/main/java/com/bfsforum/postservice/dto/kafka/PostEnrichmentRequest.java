package com.bfsforum.postservice.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-07
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostEnrichmentRequest {
	private UUID requestId;
	private List<String> postIds;
}
