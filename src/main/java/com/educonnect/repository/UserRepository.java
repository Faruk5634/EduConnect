package com.educonnect.repository;

import com.educonnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Kullanıcı adına göre veritabanında arama yapar (Giriş yaparken hayati önem taşır)
    Optional<User> findByUsername(String username);

}
