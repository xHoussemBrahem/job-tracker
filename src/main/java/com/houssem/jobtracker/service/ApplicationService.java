package com.houssem.jobtracker.service;

import com.houssem.jobtracker.dto.ApplicationDto;
import com.houssem.jobtracker.exception.ApplicationNotFoundException;
import com.houssem.jobtracker.exception.InvalidStatusTransitionException;
import com.houssem.jobtracker.model.ApplicationStatus;
import com.houssem.jobtracker.model.JobApplication;
import com.houssem.jobtracker.model.StatusHistory;
import com.houssem.jobtracker.repository.ApplicationSpecification;
import com.houssem.jobtracker.repository.JobApplicationRepository;
import com.houssem.jobtracker.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApplicationService {

    private final JobApplicationRepository applicationRepo;
    private final StatusHistoryRepository historyRepo;

    @Transactional
    public ApplicationDto.Response create(ApplicationDto.CreateRequest req) {
        JobApplication app = JobApplication.builder()
                .company(req.getCompany())
                .role(req.getRole())
                .status(ApplicationStatus.APPLIED)
                .appliedAt(req.getAppliedAt())
                .notes(req.getNotes())
                .jobUrl(req.getJobUrl())
                .location(req.getLocation())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .build();

        app = applicationRepo.save(app);

        StatusHistory initial = StatusHistory.builder()
                .application(app)
                .fromStatus(null)
                .toStatus(ApplicationStatus.APPLIED)
                .comment("Application created")
                .build();
        historyRepo.save(initial);

        log.info("Created application id={} company={}", app.getId(), app.getCompany());
        return toResponse(app);
    }

    @Transactional(readOnly = true)
    public List<ApplicationDto.Response> findAll(
            ApplicationStatus status, LocalDate from, LocalDate to,
            String company, Boolean stale) {

        Specification<JobApplication> spec = Specification
                .where(ApplicationSpecification.hasStatus(status))
                .and(ApplicationSpecification.appliedAfter(from))
                .and(ApplicationSpecification.appliedBefore(to))
                .and(ApplicationSpecification.companyContains(company))
                .and(ApplicationSpecification.isStale(stale));

        return applicationRepo.findAll(spec).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ApplicationDto.Response findById(Long id) {
        return toResponse(getOrThrow(id));
    }

    @Transactional
    public ApplicationDto.Response update(Long id, ApplicationDto.UpdateRequest req) {
        JobApplication app = getOrThrow(id);

        if (req.getCompany() != null) app.setCompany(req.getCompany());
        if (req.getRole() != null) app.setRole(req.getRole());
        if (req.getNotes() != null) app.setNotes(req.getNotes());
        if (req.getJobUrl() != null) app.setJobUrl(req.getJobUrl());
        if (req.getLocation() != null) app.setLocation(req.getLocation());
        if (req.getSalaryMin() != null) app.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) app.setSalaryMax(req.getSalaryMax());

        return toResponse(applicationRepo.save(app));
    }

    @Transactional
    public ApplicationDto.Response updateStatus(Long id, ApplicationDto.StatusUpdateRequest req) {
        JobApplication app = getOrThrow(id);
        ApplicationStatus current = app.getStatus();
        ApplicationStatus next = req.getStatus();

        if (!current.canTransitionTo(next)) {
            throw new InvalidStatusTransitionException(current, next);
        }

        app.setStatus(next);
        app.setStaleFlag(false); // reset stale flag on any activity
        applicationRepo.save(app);

        StatusHistory history = StatusHistory.builder()
                .application(app)
                .fromStatus(current)
                .toStatus(next)
                .comment(req.getComment())
                .build();
        historyRepo.save(history);

        log.info("Application id={} transitioned {} -> {}", id, current, next);
        return toResponse(app);
    }

    @Transactional
    public void delete(Long id) {
        if (!applicationRepo.existsById(id)) throw new ApplicationNotFoundException(id);
        applicationRepo.deleteById(id);
        log.info("Deleted application id={}", id);
    }

    @Transactional(readOnly = true)
    public ApplicationDto.StatsResponse getStats() {
        long total     = applicationRepo.count();
        long applied   = applicationRepo.countByStatus(ApplicationStatus.APPLIED);
        long screening = applicationRepo.countByStatus(ApplicationStatus.SCREENING);
        long interview = applicationRepo.countByStatus(ApplicationStatus.INTERVIEW);
        long offer     = applicationRepo.countByStatus(ApplicationStatus.OFFER);
        long rejected  = applicationRepo.countByStatus(ApplicationStatus.REJECTED);
        long withdrawn = applicationRepo.countByStatus(ApplicationStatus.WITHDRAWN);
        long stale     = applicationRepo.countByStaleFlag(true);
        long responses = applicationRepo.countWithResponse();

        double responseRate  = total == 0 ? 0 : (double) responses / total * 100;
        double interviewRate = responses == 0 ? 0 : (double) interview / responses * 100;

        return ApplicationDto.StatsResponse.builder()
                .total(total)
                .applied(applied)
                .screening(screening)
                .interview(interview)
                .offer(offer)
                .rejected(rejected)
                .withdrawn(withdrawn)
                .stale(stale)
                .responseRate(Math.round(responseRate * 10.0) / 10.0)
                .interviewRate(Math.round(interviewRate * 10.0) / 10.0)
                .avgDaysToResponse(applicationRepo.avgDaysToFirstResponse())
                .build();
    }

    // --- helpers ---

    private JobApplication getOrThrow(Long id) {
        return applicationRepo.findById(id)
                .orElseThrow(() -> new ApplicationNotFoundException(id));
    }

    private ApplicationDto.Response toResponse(JobApplication app) {
        List<ApplicationDto.StatusHistoryDto> history = app.getStatusHistory().stream()
                .map(h -> ApplicationDto.StatusHistoryDto.builder()
                        .fromStatus(h.getFromStatus())
                        .toStatus(h.getToStatus())
                        .changedAt(h.getChangedAt())
                        .comment(h.getComment())
                        .build())
                .toList();

        return ApplicationDto.Response.builder()
                .id(app.getId())
                .company(app.getCompany())
                .role(app.getRole())
                .status(app.getStatus())
                .appliedAt(app.getAppliedAt())
                .updatedAt(app.getUpdatedAt())
                .notes(app.getNotes())
                .jobUrl(app.getJobUrl())
                .location(app.getLocation())
                .salaryMin(app.getSalaryMin())
                .salaryMax(app.getSalaryMax())
                .staleFlag(app.isStaleFlag())
                .statusHistory(history)
                .build();
    }
}
