package com.educonnect.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Kalenin tüm kapılarına (uç noktalarına) bu kuralı uygula
                        .allowedOrigins("http://localhost:5173") // Sadece React gemisine (Vite limanı) izin ver
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // İzin verilen işlem türleri
                        .allowedHeaders("*"); // Her türlü bilet ve formata izin ver
            }
        };
    }
}
