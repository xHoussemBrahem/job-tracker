package com.houssem.jobtracker.model;

public enum ApplicationStatus {
    APPLIED,
    SCREENING,
    INTERVIEW,
    OFFER,
    REJECTED,
    WITHDRAWN;

    public boolean canTransitionTo(ApplicationStatus next) {
        return switch (this) {
            case APPLIED    -> next == SCREENING  || next == REJECTED || next == WITHDRAWN;
            case SCREENING  -> next == INTERVIEW  || next == REJECTED || next == WITHDRAWN;
            case INTERVIEW  -> next == OFFER      || next == REJECTED || next == WITHDRAWN;
            case OFFER      -> next == WITHDRAWN;
            case REJECTED, WITHDRAWN -> false;
        };
    }
}
