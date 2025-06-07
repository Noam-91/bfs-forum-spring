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
	private String firstName;
	private String lastName;
	private String email;
	private String profileImageURL;
	private String userType; // ADMIN, USER等
	
	// 显示名称的便利方法
	public String getDisplayName() {
		if (firstName != null && lastName != null) {
			return firstName + " " + lastName;
		} else if (firstName != null) {
			return firstName;
		} else if (email != null) {
			return email.split("@")[0]; // 使用邮箱前缀
		}
		return "Anonymous User";
	}
}

