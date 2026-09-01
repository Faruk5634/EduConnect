package com.educonnect.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "schoolId", type = Long.class))
@Filter(name = "tenantFilter", condition = "school_id = :schoolId")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Announcement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Teacher author;

    @Enumerated(EnumType.STRING)
    private AnnouncementType type;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "announcement_classrooms",
            joinColumns = @JoinColumn(name = "announcement_id"),
            inverseJoinColumns = @JoinColumn(name = "classroom_id")
    )
    private List<Classroom> classrooms = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id")
    @JsonIgnore
    private School school;

    @ElementCollection
    @CollectionTable(name = "announcement_files", joinColumns = @JoinColumn(name = "announcement_id"))
    private List<AnnouncementFile> attachedFiles = new ArrayList<>();
}