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
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponseDTO {
//	private boolean success;
//	private String message;
//	private T data;
//	private LocalDateTime timestamp;
//
//	// 成功响应的便利构造函数
//	public static <T> ApiResponseDTO<T> success(T data) {
//		return new ApiResponseDTO<>(true, "Operation successful", data, LocalDateTime.now());
//	}
//
//	public static <T> ApiResponseDTO<T> success(String message, T data) {
//		return new ApiResponseDTO<>(true, message, data, LocalDateTime.now());
//	}
//
//	// 错误响应的便利构造函数
//	public static <T> ApiResponseDTO<T> error(String message) {
//		return new ApiResponseDTO<>(false, message, null, LocalDateTime.now());
//	}
//
//	public static <T> ApiResponseDTO<T> error(String message, T data) {
//		return new ApiResponseDTO<>(false, message, data, LocalDateTime.now());
//	}
}
