package com.university.tarecruitment.dto;

import com.university.tarecruitment.entity.AcademicRecord;
import com.university.tarecruitment.entity.Application;
import com.university.tarecruitment.entity.ApplicationStatus;
import com.university.tarecruitment.entity.ApplicationTimeline;
import com.university.tarecruitment.entity.AttachedDocument;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class ApplicationDetailDTO {

    private Long applicationId;
    private String studentName;
    private String studentEmail;
    private String studentPhone;
    private String studentNumber;
    private String programme;
    private Integer yearOfStudy;
    private String department;

    private Double overallGpa;
    private List<AcademicRecordDTO> academicRecords;

    private String moduleCode;
    private String moduleName;
    private LocalDateTime appliedDate;
    private ApplicationStatus status;
    private Integer preferredHours;
    private String availability;

    private String personalStatement;
    private String previousExperience;
    private List<DocumentDTO> documents;

    private String reviewerNotes;
    private String reviewerName;
    private LocalDateTime reviewedDate;

    private LocalDateTime interviewDateTime;
    private String interviewRoom;
    private String interviewNotes;

    private List<TimelineDTO> timeline;

    public ApplicationDetailDTO(Application application) {
        this.applicationId = application.getApplicationId();
        this.studentName = application.getStudentName();
        this.studentEmail = application.getStudentEmail();
        this.studentPhone = application.getStudentPhone();
        this.studentNumber = application.getStudentNumber();
        this.programme = application.getProgramme();
        this.yearOfStudy = application.getYearOfStudy();
        this.department = application.getDepartment();
        this.overallGpa = application.getOverallGpa();
        this.moduleCode = application.getModuleCode();
        this.moduleName = application.getModuleName();
        this.appliedDate = application.getAppliedDate();
        this.status = application.getStatus();
        this.preferredHours = application.getPreferredHours();
        this.availability = application.getAvailability();
        this.personalStatement = application.getPersonalStatement();
        this.previousExperience = application.getPreviousExperience();
        this.reviewerNotes = application.getReviewerNotes();
        this.reviewerName = application.getReviewerName();
        this.reviewedDate = application.getReviewedDate();
        this.interviewDateTime = application.getInterviewDateTime();
        this.interviewRoom = application.getInterviewRoom();
        this.interviewNotes = application.getInterviewNotes();

        List<AcademicRecord> ar = application.getAcademicRecords();
        this.academicRecords =
                ar == null
                        ? Collections.emptyList()
                        : ar.stream().map(AcademicRecordDTO::new).collect(Collectors.toList());

        List<AttachedDocument> docs = application.getDocuments();
        this.documents =
                docs == null
                        ? Collections.emptyList()
                        : docs.stream().map(DocumentDTO::new).collect(Collectors.toList());

        List<ApplicationTimeline> tl = application.getTimeline();
        this.timeline =
                tl == null
                        ? Collections.emptyList()
                        : tl.stream().map(TimelineDTO::new).collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    public static class AcademicRecordDTO {
        private String moduleCode;
        private String moduleName;
        private String grade;
        private Integer percentage;
        private String semester;

        public AcademicRecordDTO(AcademicRecord record) {
            this.moduleCode = record.getModuleCode();
            this.moduleName = record.getModuleName();
            this.grade = record.getGrade();
            this.percentage = record.getPercentage();
            this.semester = record.getSemester();
        }
    }

    @Data
    @NoArgsConstructor
    public static class DocumentDTO {
        private Long documentId;
        private String fileName;
        private String fileType;
        private String fileSize;
        private String fileUrl;

        public DocumentDTO(AttachedDocument doc) {
            this.documentId = doc.getDocumentId();
            this.fileName = doc.getFileName();
            this.fileType = doc.getFileType();
            this.fileSize = doc.getFileSizeFormatted();
            this.fileUrl = doc.getFileUrl();
        }
    }

    @Data
    @NoArgsConstructor
    public static class TimelineDTO {
        private LocalDateTime timestamp;
        private String formattedTimestamp;
        private String action;
        private String performedBy;
        private String notes;

        public TimelineDTO(ApplicationTimeline timeline) {
            this.timestamp = timeline.getTimestamp();
            this.formattedTimestamp = timeline.getFormattedTimestamp();
            this.action = timeline.getAction();
            this.performedBy = timeline.getPerformedBy();
            this.notes = timeline.getNotes();
        }
    }
}
