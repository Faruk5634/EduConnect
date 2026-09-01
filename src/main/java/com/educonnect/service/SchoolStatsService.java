package com.educonnect.service;

import com.educonnect.dto.SchoolStatsDTO;
import com.educonnect.model.School;
import com.educonnect.model.User;
import com.educonnect.repository.AnnouncementRepository;
import com.educonnect.repository.ClassroomRepository;
import com.educonnect.repository.ParentRepository;
import com.educonnect.repository.StudentRepository;
import com.educonnect.repository.TeacherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class SchoolStatsService {

    private final UserService userService;
    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final ClassroomRepository classroomRepository;
    private final ParentRepository parentRepository;
    private final AnnouncementRepository announcementRepository;

    public SchoolStatsDTO getCurrentSchoolStats() {
        User user = userService.getCurrentUser();
        School school = user.getSchool();

        if (school == null) {
            return new SchoolStatsDTO(0, 0, 0, 0, 0);
        }

        return new SchoolStatsDTO(
                studentRepository.countBySchool(school),
                teacherRepository.countBySchool(school),
                classroomRepository.countBySchool(school),
                parentRepository.countBySchool(school),
                announcementRepository.countBySchool(school)
        );
    }
}
