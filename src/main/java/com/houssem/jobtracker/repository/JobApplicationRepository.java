package com.houssem.jobtracker.repository;

import com.houssem.jobtracker.model.ApplicationStatus;
import com.houssem.jobtracker.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long>,
        JpaSpecificationExecutor<JobApplication> {

    long countByStatus(ApplicationStatus status);

    long countByStaleFlag(boolean staleFlag);

    // applications still in APPLIED or SCREENING older than N days and not yet stale-flagged
    @Query("""
        SELECT a FROM JobApplication a
        WHERE a.status IN ('APPLIED', 'SCREENING')
        AND a.appliedAt <= :cutoff
        AND a.staleFlag = false
    """)
    List<JobApplication> findStaleApplications(LocalDate cutoff);

    // for stats: count applications that moved past APPLIED status
    @Query("SELECT COUNT(DISTINCT h.application.id) FROM StatusHistory h WHERE h.fromStatus = 'APPLIED'")
    long countWithResponse();

    // average days from appliedAt to first status change
    @Query("""
        SELECT AVG(DATEDIFF(DAY, a.appliedAt, CAST(h.changedAt AS date)))
        FROM JobApplication a
        JOIN a.statusHistory h
        WHERE h.fromStatus = 'APPLIED'
    """)
    Double avgDaysToFirstResponse();
}
