package com.educonnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EduConnectApplication {

    public static void main(String[] args) {
        // If no active profile is provided and a DB password is missing, default to 'dev' for local H2.
        String activeProfile = System.getProperty("spring.profiles.active");
        if (activeProfile == null || activeProfile.isBlank()) {
            activeProfile = System.getenv("SPRING_PROFILES_ACTIVE");
        }

        String dbPassword = System.getenv("DB_PASSWORD");
        String dbUrl = System.getenv("DB_URL");

        if ((activeProfile == null || activeProfile.isBlank()) && (dbPassword == null || dbPassword.isBlank())) {
            // If no DB password is configured, assume local development and activate dev profile (H2).
            System.setProperty("spring.profiles.active", "dev");
            System.out.println("[EduConnect] No DB_PASSWORD found and no active profile set — activating 'dev' profile for local H2.");
        }

        SpringApplication.run(EduConnectApplication.class, args);
    }

}
