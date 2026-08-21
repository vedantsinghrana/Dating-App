CREATE TABLE messages (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches (id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    prompt_id UUID REFERENCES profile_prompts (id) ON DELETE SET NULL,
    sent_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX ix_messages_match_id_sent_at ON messages (match_id, sent_at);
