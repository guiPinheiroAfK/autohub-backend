CREATE TABLE feed_posts (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id     UUID            NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    vehicle_id  UUID            REFERENCES vehicles(id) ON DELETE SET NULL,
    content     TEXT            NOT NULL,
    image_url   VARCHAR(500),
    likes       INTEGER         NOT NULL DEFAULT 0,
    created_at  TIMESTAMP       NOT NULL DEFAULT now(),
    updated_at  TIMESTAMP       NOT NULL DEFAULT now()
);

CREATE INDEX idx_feed_posts_user_id    ON feed_posts (user_id);
CREATE INDEX idx_feed_posts_created_at ON feed_posts (created_at DESC);
