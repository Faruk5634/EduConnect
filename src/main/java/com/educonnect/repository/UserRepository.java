package com.educonnect.repository;

import com.educonnect.model.Role;
import com.educonnect.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Kullanıcı adına göre veritabanında arama yapar (Giriş yaparken hayati önem taşır)
    Optional<User> findByUsername(String username);

    Optional<User> findFirstByRole(Role role);

    List<User> findByRoleIn(Collection<Role> roles);

    List<User> findBySchool_IdOrRole(Long schoolId, Role role);
}
