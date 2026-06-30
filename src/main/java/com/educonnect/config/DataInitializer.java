package com.educonnect.config;

import com.educonnect.model.Role;
import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        // Eğer veritabanında 'admin' adında bir kullanıcı yoksa, hemen oluştur
        if (userRepository.findByUsername("superadmin").isEmpty()) {
            User superAdmin = User.builder()
                    .username("superadmin")
                    .password(passwordEncoder.encode("123")) // Varsayılan şifren
                    .firstName("Super")
                    .lastName("Admin")
                    .phone("05550000000")
                    .role(Role.ROLE_SUPER_ADMIN)
                    .build();

            userRepository.save(superAdmin);
            System.out.println("🚀 SİSTEM MESAJI: Veritabanı sıfırlandı. Varsayılan Super Admin hesabı (superadmin / 123) başarıyla oluşturuldu!");
        }
    }
}