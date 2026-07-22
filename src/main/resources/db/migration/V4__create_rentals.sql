CREATE TABLE rentals (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    gear_id BIGINT NOT NULL REFERENCES gears(id),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    total_price INTEGER NOT NULL CHECK (total_price >= 0),
    status VARCHAR(30) NOT NULL DEFAULT 'RESERVED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (end_date > start_date)
);

CREATE INDEX idx_rentals_user_created_at ON rentals (user_id, created_at DESC);
