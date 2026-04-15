package com.houssem.jobtracker.repository;

import com.houssem.jobtracker.model.ApplicationStatus;
import com.houssem.jobtracker.model.JobApplication;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class ApplicationSpecification {

    public static Specification<JobApplication> hasStatus(ApplicationStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    public static Specification<JobApplication> appliedAfter(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("appliedAt"), from);
    }

    public static Specification<JobApplication> appliedBefore(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("appliedAt"), to);
    }

    public static Specification<JobApplication> companyContains(String keyword) {
        return (root, query, cb) ->
                keyword == null ? null : cb.like(cb.lower(root.get("company")), "%" + keyword.toLowerCase() + "%");
    }

    public static Specification<JobApplication> isStale(Boolean stale) {
        return (root, query, cb) ->
                stale == null ? null : cb.equal(root.get("staleFlag"), stale);
    }
}
