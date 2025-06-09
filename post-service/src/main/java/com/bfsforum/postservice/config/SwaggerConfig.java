package com.bfsforum.postservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

import java.util.List;

/**
 * @author luluxue
 * @date 2025-06-07
 */

@Configuration
public class SwaggerConfig {
//	@Value("${app.version:1.0.0}")
//	private String appVersion;
//
//	@Value("${app.name:Post Service}")
//	private String appName;
//
//	@Value("${app.description:Forum Post Management Service}")
//	private String appDescription;
//
//	@Value("${server.port:0}")
//	private String serverPort;
//
//	@Bean
//	public OpenAPI customOpenAPI() {
//		return new OpenAPI()
//				.info(apiInfo())
//				.servers(List.of(
//						new Server().url("http://localhost:" + serverPort).description("Local server"),
//						new Server().url("http://localhost:8080").description("API Gateway")
//				))
//				.components(new Components()
//						.addSecuritySchemes("Bearer Authentication", createAPIKeyScheme()))
//				.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"));
//	}
//
//	private Info apiInfo() {
//		return new Info()
//				.title(appName)
//				.description(appDescription)
//				.version(appVersion)
//				.contact(new Contact()
//						.name("Forum Development Team")
//						.email("dev@forum.com")
//						.url("https://forum.com"))
//				.license(new License()
//						.name("MIT License")
//						.url("https://opensource.org/licenses/MIT"))
//				.termsOfService("https://forum.com/terms");
//	}
//
//	private SecurityScheme createAPIKeyScheme() {
//		return new SecurityScheme()
//				.type(SecurityScheme.Type.HTTP)
//				.bearerFormat("JWT")
//				.scheme("bearer")
//				.description("Enter JWT token for authentication");
//	}
}
