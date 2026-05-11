# Job Application Tracker API

A REST API to track job applications through their full lifecycle — from initial application to offer or rejection. Built with Java 21, Spring Boot 3, PostgreSQL, and Docker.

## Features

- Full CRUD for job applications
- Status pipeline with validated transitions: `APPLIED → SCREENING → INTERVIEW → OFFER / REJECTED / WITHDRAWN`
- Complete status change history per application
- Filtering by status, date range, company name, stale flag
- Scheduled job that auto-flags applications with no response after 14 days
- Stats endpoint: response rate, interview rate, average days to first response
- Integration tests with Testcontainers (real PostgreSQL, no mocks)
- DB schema managed with Flyway migrations

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.2 |
| Database | PostgreSQL 16 |
| Migrations | Flyway |
| Containerization | Docker + Docker Compose |
| Testing | JUnit 5, Testcontainers |

## Getting Started

**Prerequisites:** Docker and Docker Compose installed.

```bash
git clone https://github.com/xHoussemBrahem/job-tracker.git
cd job-tracker
docker-compose up --build
```

API is available at `http://localhost:8080`  
pgAdmin at `http://localhost:5050` (admin@jobtracker.local / admin)

## API Reference

### Create an application
```bash
curl -X POST http://localhost:8080/api/v1/applications \
  -H "Content-Type: application/json" \
  -d '{
    "company": "COMPANY NAME",
    "role": "ROLE",
    "appliedAt": "2026-05-01",
    "location": "LOCATION",
    "jobUrl": "https://LINK/...",
    "salaryMin": 6000,
    "salaryMax": 9000,
    "notes": "Referral from LinkedIn"
  }'
```

Response:
```json
{
  "id": 1,
  "company": "COMPANY NAME",
  "role": "ROLE",
  "status": "APPLIED",
  "appliedAt": "2026-05-01",
  "updatedAt": "2026-05-01T10:23:45",
  "location": "LOCATION",
  "salaryMin": 6000,
  "salaryMax": 9000,
  "staleFlag": false,
  "statusHistory": [
    { "fromStatus": null, "toStatus": "APPLIED", "changedAt": "2026-05-01T10:23:45", "comment": "Application created" }
  ]
}
```

### Update status
```bash
curl -X PATCH http://localhost:8080/api/v1/applications/1/status \
  -H "Content-Type: application/json" \
  -d '{ "status": "SCREENING", "comment": "Recruiter reached out via LinkedIn" }'
```

### Filter applications
```bash
# All active applications
curl "http://localhost:8080/api/v1/applications?status=APPLIED"

# Applications from a specific company
curl "http://localhost:8080/api/v1/applications?company=COMPANY_NAME"

# Stale applications (no response in 14 days)
curl "http://localhost:8080/api/v1/applications?stale=true"

# Date range
curl "http://localhost:8080/api/v1/applications?from=2026-01-01&to=2026-05-01"
```

### Stats
```bash
curl http://localhost:8080/api/v1/applications/stats
```

Response:
```json
{
  "total": 24,
  "applied": 10,
  "screening": 5,
  "interview": 3,
  "offer": 1,
  "rejected": 4,
  "withdrawn": 1,
  "stale": 6,
  "responseRate": 58.3,
  "interviewRate": 42.9,
  "avgDaysToResponse": 4.7
}
```

## Status Transition Rules

```
APPLIED ──► SCREENING ──► INTERVIEW ──► OFFER
   │              │             │
   └──────────────┴─────────────┴──► REJECTED
                                 └──► WITHDRAWN
```

Invalid transitions return `422 Unprocessable Entity`.

## Design Decisions

**Why Flyway instead of `ddl-auto: create`?**  
Schema migrations should be explicit and versioned. Flyway gives a clear history of every schema change, making it safe to run in production without surprises.

**Why Testcontainers instead of H2?**  
H2 in-memory databases behave differently from PostgreSQL (type handling, constraints, query planner). Testcontainers spins up a real PostgreSQL instance per test run — what you test is exactly what runs in production.

**Why a separate `StatusHistory` table?**  
Audit trails matter in production systems. Storing only the current status loses information — when did it change, who changed it, why. The history table makes it possible to calculate response times and pipeline metrics without any guesswork.

**Why `canTransitionTo()` on the enum itself?**  
The transition rules are business logic, not controller logic. Putting them on the enum keeps them close to the data they describe and makes them easy to test in isolation.

## Running Tests

```bash
# Requires Docker running (for Testcontainers)
mvn test
```
