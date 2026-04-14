CREATE TABLE job_applications (
    id          BIGSERIAL PRIMARY KEY,
    company     VARCHAR(150)  NOT NULL,
    role        VARCHAR(150)  NOT NULL,
    status      VARCHAR(30)   NOT NULL DEFAULT 'APPLIED',
    applied_at  DATE          NOT NULL,
    updated_at  TIMESTAMP     NOT NULL DEFAULT now(),
    notes       TEXT,
    job_url     VARCHAR(500),
    location    VARCHAR(150),
    salary_min  INTEGER,
    salary_max  INTEGER,
    stale_flag  BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_applications_status  ON job_applications(status);
CREATE INDEX idx_applications_applied ON job_applications(applied_at);
