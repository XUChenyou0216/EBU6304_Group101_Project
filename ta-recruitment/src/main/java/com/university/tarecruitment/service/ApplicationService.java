package com.university.tarecruitment.service;

import com.university.tarecruitment.dto.ApplicationDetailDTO;
import com.university.tarecruitment.dto.ApplicationListDTO;
import com.university.tarecruitment.dto.ApplicationStatisticsDTO;
import com.university.tarecruitment.entity.Application;
import com.university.tarecruitment.entity.ApplicationStatus;
import com.university.tarecruitment.entity.ApplicationTimeline;
import com.university.tarecruitment.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;

    /**
     * 获取统计数据
     */
    public ApplicationStatisticsDTO getStatistics(Long reviewerId) {
        ApplicationStatisticsDTO stats = new ApplicationStatisticsDTO();

        List<Application> allApplications = applicationRepository.findByReviewerId(reviewerId);
        stats.setTotalApplicants(allApplications.size());

        stats.setPendingReview(countByStatus(allApplications, ApplicationStatus.PENDING));
        stats.setUnderReview(countByStatus(allApplications, ApplicationStatus.UNDER_REVIEW));
        stats.setInterviewed(countByStatus(allApplications, ApplicationStatus.INTERVIEWED));
        stats.setConfirmedHires(countByStatus(allApplications, ApplicationStatus.ACCEPTED));
        stats.setRejected(countByStatus(allApplications, ApplicationStatus.REJECTED));

        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        stats.setApplicantsLastWeek(
                (int) allApplications.stream()
                        .filter(a -> a.getAppliedDate() != null && a.getAppliedDate().isBefore(oneWeekAgo))
                        .count());

        int totalPos = 4;
        stats.setTotalPositions(totalPos);
        int hires = stats.getConfirmedHires() != null ? stats.getConfirmedHires() : 0;
        stats.setRemainingVacancies(totalPos - hires);

        return stats;
    }

    private Integer countByStatus(List<Application> applications, ApplicationStatus status) {
        return (int) applications.stream()
                .filter(a -> a.getStatus() == status)
                .count();
    }

    /**
     * 获取申请列表（带筛选和分页）
     */
    public Page<ApplicationListDTO> getApplications(
            Long reviewerId,
            String searchKeyword,
            Long moduleId,
            ApplicationStatus status,
            String academicYear,
            String sortBy,
            Pageable pageable) {

        Specification<Application> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("reviewerId"), reviewerId));

            if (searchKeyword != null && !searchKeyword.isEmpty()) {
                String likePattern = "%" + searchKeyword + "%";
                predicates.add(cb.or(
                        cb.like(root.get("studentName"), likePattern),
                        cb.like(root.get("studentNumber"), likePattern),
                        cb.like(root.get("studentEmail"), likePattern)));
            }

            if (moduleId != null) {
                predicates.add(cb.equal(root.get("moduleId"), moduleId));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (academicYear != null && !academicYear.isEmpty()) {
                // 根据学年过滤申请日期（可按业务规则扩展）
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Application> applications = applicationRepository.findAll(spec, pageable);

        return applications.map(ApplicationListDTO::new);
    }

    @Transactional(readOnly = true)
    public ApplicationDetailDTO getApplicationDetail(Long applicationId) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        return new ApplicationDetailDTO(application);
    }

    @Transactional
    public void updateApplicationStatus(Long applicationId, ApplicationStatus newStatus,
            String notes, Long reviewerId, String reviewerName) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        ApplicationStatus oldStatus = application.getStatus();
        application.setStatus(newStatus);
        application.setReviewerNotes(notes);
        application.setReviewerId(reviewerId);
        application.setReviewerName(reviewerName);
        application.setReviewedDate(LocalDateTime.now());

        ApplicationTimeline timeline = new ApplicationTimeline();
        timeline.setApplication(application);
        timeline.setTimestamp(LocalDateTime.now());
        timeline.setAction(String.format("Status changed from %s to %s",
                oldStatus != null ? oldStatus.getDisplayName() : "?",
                newStatus.getDisplayName()));
        timeline.setPerformedBy(reviewerName);
        timeline.setNotes(notes);

        application.getTimeline().add(timeline);

        applicationRepository.save(application);
    }

    @Transactional
    public void scheduleInterview(Long applicationId, LocalDateTime interviewDateTime,
            String interviewRoom, String reviewerName) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new RuntimeException("Application not found"));

        application.setInterviewDateTime(interviewDateTime);
        application.setInterviewRoom(interviewRoom);
        application.setStatus(ApplicationStatus.INTERVIEWED);

        ApplicationTimeline timeline = new ApplicationTimeline();
        timeline.setApplication(application);
        timeline.setTimestamp(LocalDateTime.now());
        timeline.setAction(String.format("Interview scheduled for %s in room %s",
                interviewDateTime, interviewRoom));
        timeline.setPerformedBy(reviewerName);

        application.getTimeline().add(timeline);

        applicationRepository.save(application);
    }

    @Transactional
    public void bulkUpdateStatus(List<Long> applicationIds, ApplicationStatus newStatus,
            String reviewerName) {
        List<Application> applications = applicationRepository.findAllById(applicationIds);

        for (Application application : applications) {
            application.setStatus(newStatus);
            application.setReviewedDate(LocalDateTime.now());

            ApplicationTimeline timeline = new ApplicationTimeline();
            timeline.setApplication(application);
            timeline.setTimestamp(LocalDateTime.now());
            timeline.setAction("Bulk status update to " + newStatus.getDisplayName());
            timeline.setPerformedBy(reviewerName);

            application.getTimeline().add(timeline);
        }

        applicationRepository.saveAll(applications);
    }
}
