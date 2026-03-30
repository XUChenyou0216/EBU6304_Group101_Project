package com.university.tarecruitment.repository;

import com.university.tarecruitment.entity.Application;
import com.university.tarecruitment.entity.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    List<Application> findByStatus(ApplicationStatus status);

    List<Application> findByModuleId(Long moduleId);

    List<Application> findByReviewerId(Long reviewerId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = :status")
    long countByStatus(@Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.reviewerId = :reviewerId AND a.status = :status")
    long countByReviewerIdAndStatus(@Param("reviewerId") Long reviewerId,
            @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.appliedDate >= :startDate")
    long countApplicationsSince(@Param("startDate") LocalDateTime startDate);

    @Query("SELECT a FROM Application a WHERE a.reviewerId = :reviewerId "
            + "AND (:moduleId IS NULL OR a.moduleId = :moduleId) "
            + "AND (:status IS NULL OR a.status = :status) "
            + "ORDER BY a.appliedDate DESC")
    List<Application> findFilteredApplications(
            @Param("reviewerId") Long reviewerId,
            @Param("moduleId") Long moduleId,
            @Param("status") ApplicationStatus status);

    @Query("SELECT a FROM Application a WHERE a.reviewerId = :reviewerId "
            + "ORDER BY a.overallGpa DESC")
    List<Application> findByReviewerIdOrderByGpaDesc(@Param("reviewerId") Long reviewerId);
}
