package com.bfsforum.postservice.service;

import com.bfsforum.postservice.domain.Post;
import com.bfsforum.postservice.dto.PostResponseDTO;
import org.springframework.stereotype.Component;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Component
public class PostDTOConverter {
	/**
	 * Post entity converted into REST API response
	 * */
	public PostResponseDTO toPostResponseDTO(Post post) {
		PostResponseDTO dto = new PostResponseDTO();
		dto.setId(post.getId());
		dto.setUserId(post.getUserId());
		dto.setTitle(post.getTitle());
		dto.setContent(post.getContent());
		return dto;
	}
}

