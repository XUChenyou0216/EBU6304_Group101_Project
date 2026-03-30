package com.university.tarecruitment.dto;

import com.university.tarecruitment.entity.Application;
import com.university.tarecruitment.entity.ApplicationStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class ApplicationListDTO {

    private Long applicationId;
    private String studentName;
    private String studentEmail;
    private String studentNumber;
    private String moduleCode;
    private String moduleName;
    private LocalDateTime appliedDate;
    private String relativeTime;
    private Double overallGpa;
    private ApplicationStatus status;
    private String statusDisplayName;
    private String statusBackgroundColor;
    private String statusTextColor;
    private String statusBorderColor;
    private boolean hasInterview;
    private LocalDateTime interviewDateTime;
    private boolean isHighPerformer;

    public ApplicationListDTO(Application application) {
        this.applicationId = application.getApplicationId();
        this.studentName = application.getStudentName();
        this.studentEmail = application.getStudentEmail();
        this.studentNumber = application.getStudentNumber();
        this.moduleCode = application.getModuleCode();
        this.moduleName = application.getModuleName();
        this.appliedDate = application.getAppliedDate();
        this.relativeTime = application.getRelativeAppliedTime();
        this.overallGpa = application.getOverallGpa();
        this.status = application.getStatus();
        if (application.getStatus() != null) {
            this.statusDisplayName = application.getStatus().getDisplayName();
            this.statusBackgroundColor = application.getStatus().getBackgroundColor();
            this.statusTextColor = application.getStatus().getTextColor();
            this.statusBorderColor = application.getStatus().getBorderColor();
        }
        this.hasInterview = application.getInterviewDateTime() != null;
        this.interviewDateTime = application.getInterviewDateTime();
        this.isHighPerformer = application.isHighPerformer();
    }

    public String getGpaDisplay() {
        if (overallGpa == null) {
            return "N/A";
        }
        return String.format("%.2f", overallGpa);
    }

    public String getGpaColor() {
        if (overallGpa == null) {
            return "#64748B";
        }
        if (overallGpa >= 3.5) {
            return "#16A34A";
        }
        if (overallGpa >= 3.0) {
            return "#475569";
        }
        return "#DC2626";
    }
}
