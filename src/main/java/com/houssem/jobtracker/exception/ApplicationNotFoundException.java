package com.houssem.jobtracker.exception;

public class ApplicationNotFoundException extends RuntimeException {
    public ApplicationNotFoundException(Long id) {
        super("Job application not found with id: " + id);
    }
}
