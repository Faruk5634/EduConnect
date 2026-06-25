package com.educonnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths; // 🚀 YENİ EKLENDİ

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders("*");
            }

            // 🚀 SİHİRLİ DOKUNUŞ: Klasörün bilgisayardaki TAM (Absolute) yolunu bulup Spring'e veriyoruz
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                String uploadPath = Paths.get("uploads").toAbsolutePath().toUri().toString();

                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(uploadPath); // Kusursuz GPS koordinatı!
            }
        };
    }
}