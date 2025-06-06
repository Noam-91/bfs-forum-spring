package com.bfsforum.authservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity // This enables Spring Security's web security features
public class SecurityConfig { // Renamed from previous SwaggerConfig to SecurityConfig

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(authorize -> authorize
//            // Permit access to Swagger/OpenAPI endpoints
//            .requestMatchers(
//                "/swagger-ui.html",
//                "/swagger-ui/**",
//                "/v3/api-docs/**",
//                "/v2/api-docs",
//                "/webjars/**",
//                "/actuator/**" // If you have Spring Boot Actuator and want it public
//            ).permitAll()
            .anyRequest().permitAll()
        );

    return http.build();
  }
}
