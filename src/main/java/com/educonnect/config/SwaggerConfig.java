package com.educonnect.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth"; // Bileti taşıyacak görevlinin adı

        return new OpenAPI()
                // 1. Swagger'daki tüm metotların yanına o kilit ikonunu koy
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                // 2. Kilit butonuna basıldığında açılacak kutunun ayarları (Biletin formatı JWT olacak diyoruz)
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}
