-- user_a_id/user_b_id are always stored with the lexicographically smaller UUID as
-- user_a_id, so a pair only ever produces one row regardless of match direction.
CREATE TABLE matches (
    id UUID PRIMARY KEY,
    user_a_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    user_b_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    expires_at TIMESTAMPTZ NOT NULL,
    opening_move_done BOOLEAN NOT NULL DEFAULT false,
    CONSTRAINT ux_matches_user_pair UNIQUE (user_a_id, user_b_id),
    CONSTRAINT ck_matches_ordered_pair CHECK (user_a_id < user_b_id)
);

CREATE INDEX ix_matches_user_a_id ON matches (user_a_id);
CREATE INDEX ix_matches_user_b_id ON matches (user_b_id);
