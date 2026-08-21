import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { ApiError } from '../../api/client';
import { getCachedPrompts } from '../../api/promptCache';
import type { Match } from '../../api/types';
import { getMatches } from './matchesApi';
import { OpeningMovePanel } from './OpeningMovePanel';

function isExpired(match: Match): boolean {
  return new Date(match.expiresAt).getTime() <= Date.now();
}

function expiresLabel(match: Match): string {
  const hoursLeft = (new Date(match.expiresAt).getTime() - Date.now()) / (1000 * 60 * 60);
  if (hoursLeft <= 0) return 'Expired';
  if (hoursLeft < 1) return 'Expires in <1h';
  return `Expires in ${Math.round(hoursLeft)}h`;
}

export function MatchesPage() {
  const navigate = useNavigate();
  const [matches, setMatches] = useState<Match[] | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [activeMatchId, setActiveMatchId] = useState<string | null>(null);

  useEffect(() => {
    getMatches()
      .then((res) => setMatches(res.matches))
      .catch((err) => setError(err instanceof ApiError ? err.message : 'Failed to load matches.'));
  }, []);

  function handleMatchClick(match: Match) {
    if (isExpired(match)) return;
    if (match.openingMoveDone) {
      navigate(`/chat/${match.matchId}`);
      return;
    }
    setActiveMatchId((prev) => (prev === match.matchId ? null : match.matchId));
  }

  function handleOpeningMoveSent(matchId: string) {
    setMatches((prev) =>
      prev ? prev.map((m) => (m.matchId === matchId ? { ...m, openingMoveDone: true } : m)) : prev,
    );
    setActiveMatchId(null);
    navigate(`/chat/${matchId}`);
  }

  return (
    <section className="page">
      <h1>Matches</h1>
      {error && <p className="form__error" role="alert">{error}</p>}
      {matches === null && !error && <p className="page__todo">Loading…</p>}
      {matches?.length === 0 && <p className="page__todo">No matches yet — keep swiping!</p>}

      <ul className="match-list">
        {matches?.map((match) => {
          const expired = isExpired(match);
          return (
            <li key={match.matchId} className={`match-card${expired ? ' match-card--expired' : ''}`}>
              <button
                type="button"
                className="match-card__row"
                onClick={() => handleMatchClick(match)}
                disabled={expired}
              >
                <div
                  className="match-card__photo"
                  style={{ backgroundImage: `url(${match.otherUser.photos[0]})` }}
                />
                <div className="match-card__meta">
                  <span className="match-card__name">{match.otherUser.name}</span>
                  <span className="match-card__status">
                    {expired
                      ? 'Match expired'
                      : match.openingMoveDone
                        ? 'Say hi in chat'
                        : `Pick a prompt to reply · ${expiresLabel(match)}`}
                  </span>
                </div>
              </button>

              {activeMatchId === match.matchId && !expired && !match.openingMoveDone && (
                <OpeningMovePanel
                  match={match}
                  otherPrompts={getCachedPrompts(match.otherUser.userId)}
                  onSent={() => handleOpeningMoveSent(match.matchId)}
                />
              )}
            </li>
          );
        })}
      </ul>
    </section>
  );
}
