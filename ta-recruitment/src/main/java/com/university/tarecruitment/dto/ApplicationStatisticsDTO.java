package com.university.tarecruitment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationStatisticsDTO {

    private Integer totalApplicants;
    private Integer pendingReview;
    private Integer underReview;
    private Integer interviewed;
    private Integer confirmedHires;
    private Integer rejected;
    private Integer remainingVacancies;
    private Integer totalPositions;

    private Integer applicantsLastWeek;
    private Integer pendingLastWeek;

    public Integer getApplicantsChange() {
        if (applicantsLastWeek == null) {
            return 0;
        }
        int total = totalApplicants != null ? totalApplicants : 0;
        return total - applicantsLastWeek;
    }

    public Integer getPendingChange() {
        if (pendingLastWeek == null) {
            return 0;
        }
        int pending = pendingReview != null ? pendingReview : 0;
        return pending - pendingLastWeek;
    }

    public Double getHireRate() {
        if (totalPositions == null || totalPositions == 0) {
            return 0.0;
        }
        int hires = confirmedHires != null ? confirmedHires : 0;
        return (hires / (double) totalPositions) * 100;
    }

    public Integer getProcessedApplications() {
        int total = totalApplicants != null ? totalApplicants : 0;
        int pending = pendingReview != null ? pendingReview : 0;
        return total - pending;
    }

    public Double getProcessingRate() {
        if (totalApplicants == null || totalApplicants == 0) {
            return 0.0;
        }
        return (getProcessedApplications() / (double) totalApplicants) * 100;
    }

    public List<StatCardDTO> generateStatCards() {
        List<StatCardDTO> cards = new ArrayList<>();

        int total = totalApplicants != null ? totalApplicants : 0;
        int pending = pendingReview != null ? pendingReview : 0;
        int hires = confirmedHires != null ? confirmedHires : 0;
        int remaining = remainingVacancies != null ? remainingVacancies : 0;
        int positions = totalPositions != null ? totalPositions : 0;

        cards.add(
                StatCardDTO.builder()
                        .id("total_applicants")
                        .title("Total Applicants")
                        .icon("📋")
                        .iconBackground("#DBEAFE")
                        .value(total)
                        .previousValue(applicantsLastWeek)
                        .change(String.format("%+d from last week", getApplicantsChange()))
                        .changeType(getApplicantsChange() >= 0 ? "POSITIVE" : "NEGATIVE")
                        .description("Applications received")
                        .build());

        boolean needsAttention = pending > 20;
        cards.add(
                StatCardDTO.builder()
                        .id("pending_review")
                        .title("Pending Review")
                        .icon("⏳")
                        .iconBackground("#FEF3C7")
                        .value(pending)
                        .previousValue(pendingLastWeek)
                        .change(needsAttention ? "Needs attention" : "On track")
                        .changeType(needsAttention ? "WARNING" : "NEUTRAL")
                        .description("Awaiting your review")
                        .isWarning(needsAttention)
                        .build());

        cards.add(
                StatCardDTO.builder()
                        .id("confirmed_hires")
                        .title("Confirmed Hires")
                        .icon("✅")
                        .iconBackground("#DCFCE7")
                        .value(hires)
                        .change(String.format("%d of %d positions filled", hires, positions))
                        .changeType("POSITIVE")
                        .description("TAs confirmed")
                        .maxValue(positions)
                        .progressPercentage(getHireRate())
                        .build());

        cards.add(
                StatCardDTO.builder()
                        .id("remaining_vacancies")
                        .title("Remaining Vacancies")
                        .icon("📌")
                        .iconBackground("#E9D5FF")
                        .value(remaining)
                        .change(String.format("%.0f%% capacity", getHireRate()))
                        .changeType("NEUTRAL")
                        .description("Positions remaining")
                        .build());

        return cards;
    }
}
