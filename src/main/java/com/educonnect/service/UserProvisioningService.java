package com.educonnect.service;

import com.educonnect.model.Role;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.UserRepository;
import com.educonnect.security.PasswordGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 🚀 DRY / SRP FIX: ParentService.createParentWithUser, TeacherService.createTeacherWithUser,
 * and StudentService.createStudentWithUser each independently: resolved a
 * username, fell back to a default password, encoded it, built a User,
 * assigned the admin's school, and saved it. Same job, three copies,
 * slightly different each time.
 *
 * This service now owns "provision a login account for a new person" as a
 * single responsibility. The Parent/Teacher/Student services stay focused
 * on their own domain concern (creating the profile) and delegate account
 * creation here.
 */
@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsernameService usernameService;
    private final PasswordGenerator passwordGenerator;

    /**
     * Creates and saves a new User for a person being onboarded (parent, teacher,
     * or student). If no password is supplied, a secure random one is generated
     * (see PasswordGenerator) rather than a fixed, predictable default.
     */
    public User provisionUser(String requestedUsername,
                              String requestedPassword,
                              String firstName,
                              String lastName,
                              String phone,
                              String email,
                              Role role,
                              School school) {

        String username = usernameService.resolveForCreate(requestedUsername, firstName, lastName);
        String rawPassword = (requestedPassword != null && !requestedPassword.isBlank())
                ? requestedPassword
                : passwordGenerator.generate();

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .role(role)
                .firstName(firstName)
                .lastName(lastName)
                .phone(phone)
                .email(email)
                .school(school)
                .build();

        return userRepository.save(user);
    }
}