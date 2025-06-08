package com.bfsforum.postservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Value("${app.cors.allowed-origins:http://localhost:3000}")
	private String[] allowedOrigins;
	
	@Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
	private String[] allowedMethods;
	
	@Value("${app.cors.allowed-headers:*}")
	private String[] allowedHeaders;
	
	@Value("${app.cors.max-age:3600}")
	private long maxAge;
	
	/**
	 * CORS配置
	 */
	@Override
	public void addCorsMappings(CorsRegistry registry) {
		log.info("Configuring CORS with allowed origins: {}", (Object[]) allowedOrigins);
		
		registry.addMapping("/**")
				.allowedOrigins(allowedOrigins)
				.allowedMethods(allowedMethods)
				.allowedHeaders(allowedHeaders)
				.allowCredentials(true)
				.maxAge(maxAge);
	}
	
	/**
	 * 添加拦截器
	 */
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		// 请求日志拦截器
		registry.addInterceptor(new RequestLoggingInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**");
		
		// 性能监控拦截器
		registry.addInterceptor(new PerformanceInterceptor())
				.addPathPatterns("/posts/**");
	}
	
	/**
	 * 配置HTTP消息转换器
	 */
	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		converters.add(new MappingJackson2HttpMessageConverter(objectMapper()));
	}
	
	/**
	 * 自定义ObjectMapper
	 */
	@Bean
	public ObjectMapper objectMapper() {
		ObjectMapper mapper = new ObjectMapper();
		
		// 注册Java时间模块
		mapper.registerModule(new JavaTimeModule());
		
		// 使用驼峰命名策略
		mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
		
		// 忽略未知属性
		mapper.configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		
		// 不序列化null值
		mapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL);
		
		return mapper;
	}
}
