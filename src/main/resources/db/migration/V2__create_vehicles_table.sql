CREATE TABLE vehicles (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    make        VARCHAR(50)     NOT NULL,
    model       VARCHAR(50)     NOT NULL,
    year        SMALLINT        NOT NULL,
    category    VARCHAR(30)     NOT NULL,
    color       VARCHAR(30),
    plate       VARCHAR(20),
    description TEXT,
    cover_url   VARCHAR(500),
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_vehicles_user_id ON vehicles (user_id);
