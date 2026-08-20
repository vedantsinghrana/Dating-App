import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ApiError } from '../../api/client';
import type { Match } from '../../api/types';
import { getMatches } from '../matches/matchesApi';

export function ChatIndexPage() {
  const [matches, setMatches] = useState<Match[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    getMatches()
      .then((res) => setMatches(res.matches.filter((m) => m.openingMoveDone)))
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Failed to load chats.'));
  }, []);

  return (
    <section className="page">
      <h1>Chat</h1>
      {error && <p className="form__error" role="alert">{error}</p>}
      {matches === null && !error && <p className="page__todo">Loading…</p>}
      {matches?.length === 0 && (
        <p className="page__todo">
          No conversations yet. Head to <Link to="/matches">Matches</Link> and reply to a prompt to
          start one.
        </p>
      )}

      <ul className="match-list">
        {matches?.map((match) => (
          <li key={match.matchId} className="match-card">
            <Link className="match-card__row" to={`/chat/${match.matchId}`}>
              <div
                className="match-card__photo"
                style={{ backgroundImage: `url(${match.otherUser.photos[0]})` }}
              />
              <div className="match-card__meta">
                <span className="match-card__name">{match.otherUser.name}</span>
              </div>
            </Link>
          </li>
        ))}
      </ul>
    </section>
  );
}
