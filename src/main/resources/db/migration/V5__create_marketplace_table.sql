CREATE TABLE listings (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    seller_id   UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id  UUID            REFERENCES vehicles(id) ON DELETE SET NULL,
    title       VARCHAR(150)    NOT NULL,
    description TEXT,
    price       NUMERIC(12, 2)  NOT NULL,
    category    VARCHAR(50)     NOT NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE',
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_listings_seller_id ON listings (seller_id);
CREATE INDEX idx_listings_status    ON listings (status);
