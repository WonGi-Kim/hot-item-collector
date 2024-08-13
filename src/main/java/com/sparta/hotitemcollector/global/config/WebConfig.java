package com.sparta.hotitemcollector.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://hotitemcollector.com:63342", "http://hotitemcollector.com:8081", "http://localhost:8081", "http://localhost:63342")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .exposedHeaders("access", "refresh") // Expose specific headers
                .allowCredentials(true);
    }
}
