CREATE TABLE profile_photos (
    id UUID PRIMARY KEY,
    profile_id UUID NOT NULL REFERENCES profiles (id) ON DELETE CASCADE,
    url TEXT NOT NULL,
    position INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_profile_photos_profile_id ON profile_photos (profile_id);
