package com.educonnect.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Classroom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Sınıf adı boş bırakılamaz!")
    private String name;

    private Integer gradeLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private Teacher homeroomTeacher;

    @OneToMany(mappedBy = "classroom", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Student> students;

    // 🐛 BUG FIX: this used to be @OneToMany with NO mappedBy, which made
    // Hibernate treat it as an entirely separate relationship and create a
    // SECOND, disconnected join table — completely disconnected from
    // Announcement.classrooms (the actual owning side, backed by the
    // announcement_classrooms table). In practice this meant
    // ClassroomService.addAnnouncementToClass() was writing to a join table
    // that nothing else ever read from.
    //
    // This is genuinely a many-to-many (one announcement can target many
    // classrooms; one classroom can have many announcements), so the fix is
    // to make this the mapped (inverse) side of the SAME relationship that
    // Announcement.classrooms owns. Because this is now an inverse side,
    // changes must be made through the owning side (Announcement.classrooms)
    // for them to be persisted — see the updated ClassroomService.
    @ManyToMany(mappedBy = "classrooms", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Announcement> announcements = new ArrayList<>();
}