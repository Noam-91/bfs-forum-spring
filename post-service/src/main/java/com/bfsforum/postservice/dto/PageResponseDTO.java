package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageResponseDTO<T> {
	
	private List<T> content;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean last;  // is last page
	private boolean first;  // is first page
	private boolean empty;
	
	public PageResponseDTO(List<T> content, int page, int size, long totalElements) {
		this.content = content;
		this.page = page;
		this.size = size;
		this.totalElements = totalElements;
		this.totalPages = (int) Math.ceil((double) totalElements / size);
		this.last = (page >= totalPages - 1);
		this.first = (page == 0);
		this.empty = (content == null || content.isEmpty());
	}
	
}
