CREATE TABLE swipes (
    id UUID PRIMARY KEY,
    swiper_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    swipee_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    direction VARCHAR(4) NOT NULL CHECK (direction IN ('LIKE', 'PASS')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT ux_swipes_swiper_swipee UNIQUE (swiper_id, swipee_id),
    CONSTRAINT ck_swipes_no_self_swipe CHECK (swiper_id <> swipee_id)
);

CREATE INDEX ix_swipes_swipee_id ON swipes (swipee_id);
