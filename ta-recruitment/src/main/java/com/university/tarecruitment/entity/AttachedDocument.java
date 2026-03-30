package com.university.tarecruitment.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "attached_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttachedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long documentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private Application application;

    @Column(nullable = false, length = 255)
    private String fileName;

    @Column(nullable = false, length = 50)
    private String fileType;

    @Column(nullable = false)
    private Long fileSizeBytes;

    @Column(nullable = false, length = 500)
    private String fileUrl;

    @Column(nullable = false)
    private LocalDateTime uploadedDate;

    @PrePersist
    protected void onCreate() {
        if (uploadedDate == null) {
            uploadedDate = LocalDateTime.now();
        }
    }

    public String getFileSizeFormatted() {
        long kb = fileSizeBytes / 1024;
        if (kb < 1024) {
            return kb + " KB";
        }
        return String.format("%.2f MB", kb / 1024.0);
    }
}
