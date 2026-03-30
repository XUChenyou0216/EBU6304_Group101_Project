package com.university.tarecruitment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "academic_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AcademicRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 20)
    private String moduleCode;

    @Column(nullable = false, length = 200)
    private String moduleName;

    @Column(length = 5)
    private String grade;

    @Column
    private Integer percentage;

    @Column(length = 50)
    private String semester;

    @Column
    private Integer year;

    public boolean isExcellent() {
        return percentage != null && percentage >= 85;
    }
}
