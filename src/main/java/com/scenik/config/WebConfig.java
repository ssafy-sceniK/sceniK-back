package com.scenik.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final String[] allowedOrigins;
    private final String uploadDirectory;

    public WebConfig(
            @Value("${scenik.cors.allowed-origins}") String allowedOrigins,
            @Value("${scenik.upload-dir:uploads}") String uploadDirectory
    ) {
        this.allowedOrigins = Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toArray(String[]::new);
        if (this.allowedOrigins.length == 0) {
            throw new IllegalStateException("scenik.cors.allowed-origins must contain at least one origin");
        }
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("Authorization", "Content-Type")
                .exposedHeaders("X-Total-Count", "X-Unread-Count");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadRoot = Paths.get(uploadDirectory).toAbsolutePath().normalize();
        String uploadLocation = uploadRoot.toUri().toString();
        if (!uploadLocation.endsWith("/")) {
            uploadLocation = uploadLocation + "/";
        }
        registry.addResourceHandler("/api/uploads/**")
                .addResourceLocations(uploadLocation);
    }
}
