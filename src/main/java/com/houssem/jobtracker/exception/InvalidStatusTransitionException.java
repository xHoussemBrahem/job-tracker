package com.houssem.jobtracker.exception;

import com.houssem.jobtracker.model.ApplicationStatus;

public class InvalidStatusTransitionException extends RuntimeException {
    public InvalidStatusTransitionException(ApplicationStatus from, ApplicationStatus to) {
        super("Cannot transition from " + from + " to " + to);
    }
}
