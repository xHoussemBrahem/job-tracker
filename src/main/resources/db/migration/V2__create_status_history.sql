CREATE TABLE status_history (
    id             BIGSERIAL PRIMARY KEY,
    application_id BIGINT       NOT NULL REFERENCES job_applications(id) ON DELETE CASCADE,
    from_status    VARCHAR(30),
    to_status      VARCHAR(30)  NOT NULL,
    changed_at     TIMESTAMP    NOT NULL DEFAULT now(),
    comment        TEXT
);

CREATE INDEX idx_history_application ON status_history(application_id);
