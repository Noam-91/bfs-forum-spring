package com.bfsforum.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoDTO {
	private Long userId;
	private String username;
	private String profileImageURL;
	private String role; // ADMIN, USER等
	
	// 显示名称的便利方法
	public String getDisplayName() {
		if (username != null) {
			return username.split("@")[0]; // 使用邮箱前缀
		}
		return "Anonymous User";
	}
}

