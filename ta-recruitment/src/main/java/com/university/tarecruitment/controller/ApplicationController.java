package com.university.tarecruitment.controller;

import com.ta.model.User;
import com.university.tarecruitment.dto.ApplicationDetailDTO;
import com.university.tarecruitment.dto.ApplicationListDTO;
import com.university.tarecruitment.dto.ApplicationStatisticsDTO;
import com.university.tarecruitment.dto.StatCardDTO;
import com.university.tarecruitment.entity.ApplicationStatus;
import com.university.tarecruitment.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/mo/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private static final String SESSION_USER = "currentUser";

    private final ApplicationService applicationService;

    private Long resolveReviewerId(HttpSession session) {
        Object raw = session.getAttribute("userId");
        if (raw instanceof Long) {
            return (Long) raw;
        }
        if (raw instanceof Number) {
            return ((Number) raw).longValue();
        }
        User user = (User) session.getAttribute(SESSION_USER);
        if (user == null || user.getUserId() == null) {
            return null;
        }
        try {
            String digits = user.getUserId().replaceAll("\\D", "");
            if (!digits.isEmpty()) {
                return Long.parseLong(digits);
            }
        } catch (NumberFormatException ignored) {
            // fall through
        }
        return (long) user.getUserId().hashCode();
    }

    private String resolveReviewerName(HttpSession session) {
        Object n = session.getAttribute("userName");
        if (n instanceof String && !((String) n).isEmpty()) {
            return (String) n;
        }
        User user = (User) session.getAttribute(SESSION_USER);
        return user != null ? user.getUsername() : "";
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<StatCardDTO>> getStatistics(HttpSession session) {
        Long reviewerId = resolveReviewerId(session);
        if (reviewerId == null) {
            return ResponseEntity.status(401).build();
        }
        ApplicationStatisticsDTO stats = applicationService.getStatistics(reviewerId);
        List<StatCardDTO> cards = stats.generateStatCards();
        return ResponseEntity.ok(cards);
    }

    @GetMapping
    public ResponseEntity<Page<ApplicationListDTO>> getApplications(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long moduleId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String academicYear,
            @RequestParam(defaultValue = "appliedDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        Long reviewerId = resolveReviewerId(session);
        if (reviewerId == null) {
            return ResponseEntity.status(401).build();
        }

        Sort.Direction direction =
                sortDirection.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        ApplicationStatus statusEnum = null;
        if (status != null && !status.isEmpty() && !status.equals("ALL")) {
            statusEnum = ApplicationStatus.fromString(status);
        }

        Page<ApplicationListDTO> applications =
                applicationService.getApplications(
                        reviewerId, search, moduleId, statusEnum, academicYear, sortBy, pageable);

        return ResponseEntity.ok(applications);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationDetailDTO> getApplicationDetail(@PathVariable Long id) {
        ApplicationDetailDTO detail = applicationService.getApplicationDetail(id);
        return ResponseEntity.ok(detail);
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        Long reviewerId = resolveReviewerId(session);
        if (reviewerId == null) {
            return ResponseEntity.status(401).build();
        }
        String reviewerName = resolveReviewerName(session);

        ApplicationStatus newStatus = ApplicationStatus.fromString(request.get("status"));
        String notes = request.get("notes");

        applicationService.updateApplicationStatus(id, newStatus, notes, reviewerId, reviewerName);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/interview")
    public ResponseEntity<Void> scheduleInterview(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        String reviewerName = resolveReviewerName(session);

        LocalDateTime interviewDateTime = LocalDateTime.parse(request.get("dateTime"));
        String interviewRoom = request.get("room");

        applicationService.scheduleInterview(id, interviewDateTime, interviewRoom, reviewerName);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/bulk/status")
    public ResponseEntity<Void> bulkUpdateStatus(
            @RequestBody Map<String, Object> request, HttpSession session) {

        String reviewerName = resolveReviewerName(session);

        @SuppressWarnings("unchecked")
        List<Object> rawIds = (List<Object>) request.get("applicationIds");
        List<Long> applicationIds = new ArrayList<>();
        if (rawIds != null) {
            for (Object o : rawIds) {
                if (o instanceof Number) {
                    applicationIds.add(((Number) o).longValue());
                }
            }
        }
        ApplicationStatus newStatus =
                ApplicationStatus.fromString((String) request.get("status"));

        applicationService.bulkUpdateStatus(applicationIds, newStatus, reviewerName);

        return ResponseEntity.ok().build();
    }
}
