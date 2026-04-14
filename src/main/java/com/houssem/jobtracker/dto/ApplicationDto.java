package com.houssem.jobtracker.dto;

import com.houssem.jobtracker.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ApplicationDto {

    @Data
    @Builder
    public static class CreateRequest {
        @NotBlank(message = "Company name is required")
        @Size(max = 150)
        private String company;

        @NotBlank(message = "Role is required")
        @Size(max = 150)
        private String role;

        @NotNull(message = "Applied date is required")
        @PastOrPresent
        private LocalDate appliedAt;

        private String notes;
        private String jobUrl;
        private String location;
        private Integer salaryMin;
        private Integer salaryMax;
    }

    @Data
    @Builder
    public static class UpdateRequest {
        @Size(max = 150)
        private String company;

        @Size(max = 150)
        private String role;

        private String notes;
        private String jobUrl;
        private String location;
        private Integer salaryMin;
        private Integer salaryMax;
    }

    @Data
    @Builder
    public static class StatusUpdateRequest {
        @NotNull(message = "New status is required")
        private ApplicationStatus status;
        private String comment;
    }

    @Data
    @Builder
    public static class Response {
        private Long id;
        private String company;
        private String role;
        private ApplicationStatus status;
        private LocalDate appliedAt;
        private LocalDateTime updatedAt;
        private String notes;
        private String jobUrl;
        private String location;
        private Integer salaryMin;
        private Integer salaryMax;
        private boolean staleFlag;
        private List<StatusHistoryDto> statusHistory;
    }

    @Data
    @Builder
    public static class StatusHistoryDto {
        private ApplicationStatus fromStatus;
        private ApplicationStatus toStatus;
        private LocalDateTime changedAt;
        private String comment;
    }

    @Data
    @Builder
    public static class StatsResponse {
        private long total;
        private long applied;
        private long screening;
        private long interview;
        private long offer;
        private long rejected;
        private long withdrawn;
        private long stale;
        private double responseRate;       // % that moved past APPLIED
        private double interviewRate;      // % of responses that got to INTERVIEW
        private Double avgDaysToResponse;  // average days from apply to first status change
    }
}
