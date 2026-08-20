CREATE TABLE profiles (
    id UUID PRIMARY KEY REFERENCES users (id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    birthdate DATE NOT NULL,
    bio TEXT,
    location geography(Point, 4326),
    search_radius_km INTEGER NOT NULL DEFAULT 25,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX gix_profiles_location ON profiles USING GIST (location);
