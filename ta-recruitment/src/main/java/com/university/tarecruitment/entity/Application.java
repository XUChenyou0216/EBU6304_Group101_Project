package com.university.tarecruitment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long applicationId;

    @Column(nullable = false)
    private Long studentId;

    @Column(nullable = false, length = 100)
    private String studentName;

    @Column(nullable = false, length = 100)
    private String studentEmail;

    @Column(length = 20)
    private String studentPhone;

    @Column(nullable = false, length = 20)
    private String studentNumber;

    @Column(length = 200)
    private String programme;

    @Column(nullable = false)
    private Integer yearOfStudy;

    @Column(length = 100)
    private String department;

    @Column(precision = 3, scale = 2)
    private Double overallGpa;

    @Column(nullable = false)
    private Long moduleId;

    @Column(nullable = false, length = 20)
    private String moduleCode;

    @Column(nullable = false, length = 200)
    private String moduleName;

    @Column(nullable = false)
    private Long positionId;

    @Column(nullable = false)
    private LocalDateTime appliedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column
    private Integer preferredHours;

    @Column(length = 500)
    private String availability;

    @Column(columnDefinition = "TEXT")
    private String personalStatement;

    @Column(columnDefinition = "TEXT")
    private String previousExperience;

    @Column(columnDefinition = "TEXT")
    private String reviewerNotes;

    @Column
    private Long reviewerId;

    @Column(length = 100)
    private String reviewerName;

    @Column
    private LocalDateTime reviewedDate;

    @Column
    private LocalDateTime interviewDateTime;

    @Column(length = 100)
    private String interviewRoom;

    @Column(columnDefinition = "TEXT")
    private String interviewNotes;

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AcademicRecord> academicRecords = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttachedDocument> documents = new ArrayList<>();

    @OneToMany(mappedBy = "application", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ApplicationTimeline> timeline = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (appliedDate == null) {
            appliedDate = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public String getRelativeAppliedTime() {
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                appliedDate.toLocalDate(),
                LocalDateTime.now().toLocalDate());

        if (daysBetween == 0) {
            return "Today";
        }
        if (daysBetween == 1) {
            return "Yesterday";
        }
        return daysBetween + " days ago";
    }

    public boolean isHighPerformer() {
        return overallGpa != null && overallGpa >= 3.5;
    }
}
