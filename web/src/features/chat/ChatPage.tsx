import { type FormEvent, useEffect, useRef, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { connectMatchChat, type MatchChatConnection } from '../../api/chatSocket';
import { useSession } from '../../api/session';
import type { Match, Message } from '../../api/types';
import { getMatches } from '../matches/matchesApi';
import { getMessages, sendMessage } from './chatApi';

export function ChatPage() {
  const { matchId } = useParams<{ matchId: string }>();
  const session = useSession();
  const [match, setMatch] = useState<Match | null>(null);
  const [messages, setMessages] = useState<Message[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [content, setContent] = useState('');
  const [sending, setSending] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!matchId) return;

    getMatches()
      .then((res) => setMatch(res.matches.find((m) => m.matchId === matchId) ?? null))
      .catch(() => undefined);

    getMessages(matchId)
      .then((res) => setMessages(res.messages))
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Failed to load messages.'));

    let connection: MatchChatConnection | null = null;
    try {
      connection = connectMatchChat(matchId, (incoming) => {
        setMessages((prev) => (prev.some((m) => m.id === incoming.id) ? prev : [...prev, incoming]));
      });
    } catch {
      // Realtime updates are a nice-to-have here — REST history + send still work without it.
    }

    return () => connection?.disconnect();
  }, [matchId]);

  useEffect(() => {
    bottomRef.current?.scrollIntoView?.({ behavior: 'smooth' });
  }, [messages]);

  async function handleSend(event: FormEvent) {
    event.preventDefault();
    if (!matchId || !content.trim()) return;
    setSending(true);
    setError(null);
    try {
      const message = await sendMessage(matchId, { content: content.trim(), promptId: null });
      setMessages((prev) => (prev.some((m) => m.id === message.id) ? prev : [...prev, message]));
      setContent('');
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to send message.');
    } finally {
      setSending(false);
    }
  }

  return (
    <section className="page chat-page">
      <div className="chat-page__header">
        <Link to="/chat" aria-label="Back to chats" className="chat-page__back">
          ←
        </Link>
        <h1>{match?.otherUser.name ?? 'Chat'}</h1>
      </div>

      {error && <p className="form__error" role="alert">{error}</p>}

      <div className="chat-thread">
        {messages.map((message) => (
          <div
            key={message.id}
            className={`chat-bubble${message.senderId === session?.userId ? ' chat-bubble--mine' : ''}`}
          >
            {message.content}
          </div>
        ))}
        <div ref={bottomRef} />
      </div>

      <form className="chat-composer" onSubmit={handleSend}>
        <input
          type="text"
          placeholder="Message…"
          value={content}
          onChange={(e) => setContent(e.target.value)}
          aria-label="Message"
        />
        <button type="submit" className="button button--primary" disabled={sending || !content.trim()}>
          Send
        </button>
      </form>
    </section>
  );
}
