import { type FormEvent, useState } from 'react';
import { ApiError } from '../../api/client';
import type { Match, Prompt } from '../../api/types';
import { sendMessage } from '../chat/chatApi';

interface OpeningMovePanelProps {
  match: Match;
  otherPrompts: Prompt[] | null;
  onSent: () => void;
}

// The backend rejects a match's first message unless it carries a promptId
// (API_CONTRACT.md's "opening move" rule). This panel makes that the only
// path available: pick a prompt, then reply to it — never a bare text box.
export function OpeningMovePanel({ match, otherPrompts, onSent }: OpeningMovePanelProps) {
  const [selectedPrompt, setSelectedPrompt] = useState<Prompt | null>(null);
  const [content, setContent] = useState('');
  const [sending, setSending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  async function handleSend(event: FormEvent) {
    event.preventDefault();
    if (!selectedPrompt || !content.trim()) return;
    setSending(true);
    setError(null);
    try {
      await sendMessage(match.matchId, { content: content.trim(), promptId: selectedPrompt.id });
      onSent();
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to send your message.');
    } finally {
      setSending(false);
    }
  }

  if (otherPrompts === null) {
    return (
      <div className="opening-move">
        <p className="form__hint">
          {match.otherUser.name}'s prompts aren't loaded in this session yet — visit Discover
          first, then come back here to reply to one of them.
        </p>
      </div>
    );
  }

  if (!selectedPrompt) {
    return (
      <div className="opening-move">
        <p className="form__hint">Reply to one of {match.otherUser.name}'s prompts to say hi:</p>
        <div className="opening-move__prompts">
          {otherPrompts.map((prompt) => (
            <button
              key={prompt.id}
              type="button"
              className="opening-move__prompt"
              onClick={() => setSelectedPrompt(prompt)}
            >
              <span className="opening-move__prompt-q">{prompt.question}</span>
              <span className="opening-move__prompt-a">{prompt.answer}</span>
            </button>
          ))}
        </div>
      </div>
    );
  }

  return (
    <form className="opening-move opening-move__reply" onSubmit={handleSend}>
      <p className="form__hint">
        Replying to “{selectedPrompt.question}” — {selectedPrompt.answer}
      </p>
      <textarea
        autoFocus
        rows={3}
        placeholder={`Say something about ${match.otherUser.name}'s answer…`}
        value={content}
        onChange={(e) => setContent(e.target.value)}
      />
      {error && <p className="form__error" role="alert">{error}</p>}
      <div className="opening-move__reply-actions">
        <button type="button" className="button button--secondary" onClick={() => setSelectedPrompt(null)}>
          Back
        </button>
        <button type="submit" className="button button--primary" disabled={sending || !content.trim()}>
          {sending ? 'Sending…' : 'Send'}
        </button>
      </div>
    </form>
  );
}
