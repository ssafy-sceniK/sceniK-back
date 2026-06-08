package com.scenik.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI scenikOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("sceniK Backend API")
                        .version("0.1.0")
                        .description("Spring Boot APIs for sceniK Week 2-3 integration readiness."));
    }
}
