package com.houssem.jobtracker.scheduler;

import com.houssem.jobtracker.model.JobApplication;
import com.houssem.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class StaleApplicationScheduler {

    private final JobApplicationRepository applicationRepo;

    @Value("${app.stale-application-days}")
    private int staleDays;

    // runs every day at 8am
    @Scheduled(cron = "0 0 8 * * *")
    @Transactional
    public void flagStaleApplications() {
        LocalDate cutoff = LocalDate.now().minusDays(staleDays);
        List<JobApplication> stale = applicationRepo.findStaleApplications(cutoff);

        if (stale.isEmpty()) {
            log.info("Stale check: no new stale applications found");
            return;
        }

        stale.forEach(app -> app.setStaleFlag(true));
        applicationRepo.saveAll(stale);
        log.info("Stale check: flagged {} applications as stale (cutoff={})", stale.size(), cutoff);
    }
}
