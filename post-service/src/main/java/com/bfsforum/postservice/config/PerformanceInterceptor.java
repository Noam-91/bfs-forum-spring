package com.bfsforum.postservice.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * @author luluxue
 * @date 2025-06-08
 */

@Slf4j
@Component
public class PerformanceInterceptor implements HandlerInterceptor {
	private static final String PERFORMANCE_START_TIME = "performanceStartTime";
	private static final long SLOW_REQUEST_THRESHOLD = 1000; // 1秒
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		request.setAttribute(PERFORMANCE_START_TIME, System.currentTimeMillis());
		return true;
	}
	
	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
	                            Object handler, Exception ex) {
		Long startTime = (Long) request.getAttribute(PERFORMANCE_START_TIME);
		if (startTime != null) {
			long duration = System.currentTimeMillis() - startTime;
			
			if (duration > SLOW_REQUEST_THRESHOLD) {
				log.warn("Slow request detected - URI: {}, Duration: {}ms",
						request.getRequestURI(), duration);
			}
			
			// 可以在这里添加性能指标收集逻辑
			collectPerformanceMetrics(request.getRequestURI(), duration);
		}
	}
	
	private void collectPerformanceMetrics(String uri, long duration) {
		// 这里可以集成Micrometer或其他监控工具
		log.debug("Performance metric - URI: {}, Duration: {}ms", uri, duration);
	}
}
