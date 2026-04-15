package com.houssem.jobtracker.controller;

import com.houssem.jobtracker.dto.ApplicationDto;
import com.houssem.jobtracker.model.ApplicationStatus;
import com.houssem.jobtracker.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService service;

    @PostMapping
    public ResponseEntity<ApplicationDto.Response> create(
            @Valid @RequestBody ApplicationDto.CreateRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @GetMapping
    public List<ApplicationDto.Response> findAll(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Boolean stale) {
        return service.findAll(status, from, to, company, stale);
    }

    @GetMapping("/{id}")
    public ApplicationDto.Response findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PatchMapping("/{id}")
    public ApplicationDto.Response update(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDto.UpdateRequest req) {
        return service.update(id, req);
    }

    @PatchMapping("/{id}/status")
    public ApplicationDto.Response updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationDto.StatusUpdateRequest req) {
        return service.updateStatus(id, req);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ApplicationDto.StatsResponse getStats() {
        return service.getStats();
    }
}
