package com.bfsforum.messageservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig { // Renamed from SwaggerConfig for clarity

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Auth Service API") // Your application's title
            .version("1.0")           // Your API version
            .description("API documentation for the Auth Service.")
            .contact(new Contact()
                .name("Your Name/Team")
                .email("your.email@example.com")));
  }
}
