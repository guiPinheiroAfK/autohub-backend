CREATE TABLE builds (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    vehicle_id  UUID            NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE,
    title       VARCHAR(100)    NOT NULL,
    description TEXT,
    stage       VARCHAR(30)     NOT NULL DEFAULT 'PLANNING',
    completed   BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_builds_vehicle_id ON builds (vehicle_id);
