CREATE TABLE daily_top_picks (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    picked_user_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    pick_date DATE NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_daily_top_picks_user_date UNIQUE (user_id, pick_date)
);
