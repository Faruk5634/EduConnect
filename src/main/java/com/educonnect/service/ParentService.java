package com.educonnect.service;

import com.educonnect.dto.ParentDTO;
import com.educonnect.dto.ParentRequest;
import com.educonnect.exception.ResourceNotFoundException;
import com.educonnect.mapper.ParentMapper;
import com.educonnect.model.Parent;
import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ParentService {

    private final ParentRepository parentRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final UsernameService usernameService;
    private final UserProvisioningService userProvisioningService; // 🚀 replaces duplicated account-creation logic

    private School getTenantSchool(User user) {
        if (user.getRole() == Role.ROLE_SUPER_ADMIN) {
            return null;
        }
        if (user.getSchool() == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Bu kullanıcının atanmış bir okulu yok.");
        }
        return user.getSchool();
    }

    private void assertParentBelongsToTenant(Parent parent, School tenantSchool) {
        if (tenantSchool == null) {
            return;
        }
        if (parent.getUser() == null || parent.getUser().getSchool() == null || !tenantSchool.getId().equals(parent.getUser().getSchool().getId())) {
            throw new ResourceNotFoundException("Veli bulunamadı!");
        }
    }

    public Parent createParentWithUser(ParentRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);

        User savedUser = userProvisioningService.provisionUser(
                request.getUsername(),
                request.getPassword(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getEmail(),
                Role.ROLE_PARENT,
                tenantSchool != null ? tenantSchool : admin.getSchool()
        );

        Parent parent = new Parent();
        parent.setFirstName(request.getFirstName());
        parent.setLastName(request.getLastName());
        parent.setEmail(request.getEmail());
        parent.setPhoneNumber(request.getPhoneNumber());
        parent.setUser(savedUser);

        return parentRepository.save(parent);
    }

    public List<ParentDTO> getAllParents() {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        List<Parent> parents = tenantSchool != null ? parentRepository.findByUserSchool(tenantSchool) : parentRepository.findAll();
        return parents.stream().map(ParentMapper::toDto).collect(Collectors.toList());
    }

    public void deleteParent(Long id) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Parent parent = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı!"));
        assertParentBelongsToTenant(parent, tenantSchool);
        parentRepository.delete(parent);
    }

    public void updateParent(Long id, ParentRequest request) {
        User admin = userService.getCurrentUser();
        School tenantSchool = getTenantSchool(admin);
        Parent existing = parentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veli bulunamadı!"));
        assertParentBelongsToTenant(existing, tenantSchool);

        existing.setFirstName(request.getFirstName());
        existing.setLastName(request.getLastName());
        existing.setEmail(request.getEmail());
        existing.setPhoneNumber(request.getPhoneNumber());

        if (existing.getUser() != null) {
            User user = existing.getUser();
            user.setFirstName(request.getFirstName());
            user.setLastName(request.getLastName());

            if (request.getUsername() != null && !request.getUsername().isBlank()) {
                usernameService.assertUsernameAvailable(request.getUsername(), user.getId());
                user.setUsername(request.getUsername());
            }
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }

            userRepository.save(user);
        }

        parentRepository.save(existing);
    }

    public ParentDTO getParentProfileByUsername(String username) {
        Parent parent = parentRepository.findByUserUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Bu kullanıcıya ait veli profili bulunamadı!"));
        return ParentMapper.toDto(parent);
    }
}
