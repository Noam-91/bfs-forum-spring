package com.bfsforum.postservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

/**
 * @author luluxue
 * @date 2025-06-08
 */

@Slf4j
@Component
public class RequestLoggingInterceptor implements HandlerInterceptor {
	private static final String REQUEST_ID_HEADER = "X-Request-ID";
	private static final String REQUEST_START_TIME = "requestStartTime";
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		// 生成请求ID
		String requestId = request.getHeader(REQUEST_ID_HEADER);
		if (requestId == null) {
			requestId = UUID.randomUUID().toString();
		}
		
		// 设置响应头
		response.setHeader(REQUEST_ID_HEADER, requestId);
		
		// 记录请求开始时间
		request.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
		
		// 记录请求信息
		log.info("Request started - ID: {}, Method: {}, URI: {}, IP: {}",
				requestId, request.getMethod(), request.getRequestURI(), getClientIP(request));
		
		return true;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
	                            Object handler, Exception ex) {
		Long startTime = (Long) request.getAttribute(REQUEST_START_TIME);
		if (startTime != null) {
			long duration = System.currentTimeMillis() - startTime;
			String requestId = response.getHeader(REQUEST_ID_HEADER);
			
			log.info("Request completed - ID: {}, Status: {}, Duration: {}ms",
					requestId, response.getStatus(), duration);
			
			if (ex != null) {
				log.error("Request failed - ID: {}, Error: {}", requestId, ex.getMessage());
			}
		}
	}
	
	private String getClientIP(HttpServletRequest request) {
		String xForwardedFor = request.getHeader("X-Forwarded-For");
		if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
			return xForwardedFor.split(",")[0].trim();
		}
		
		String xRealIP = request.getHeader("X-Real-IP");
		if (xRealIP != null && !xRealIP.isEmpty()) {
			return xRealIP;
		}
		
		return request.getRemoteAddr();
	}
}
