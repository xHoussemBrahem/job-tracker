package com.houssem.jobtracker.repository;

import com.houssem.jobtracker.model.StatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StatusHistoryRepository extends JpaRepository<StatusHistory, Long> {
    List<StatusHistory> findByApplicationIdOrderByChangedAtAsc(Long applicationId);
}
