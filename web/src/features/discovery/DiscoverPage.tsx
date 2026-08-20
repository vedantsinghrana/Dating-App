import { useCallback, useEffect, useRef, useState } from 'react';
import { ApiError } from '../../api/client';
import { cachePrompts } from '../../api/promptCache';
import type { DiscoverResult, SwipeDirection } from '../../api/types';
import { DEFAULT_RADIUS_KM, getDiscoverResults, swipe } from './discoveryApi';
import { MatchCelebration } from './MatchCelebration';
import { SwipeCard } from './SwipeCard';

const PREFETCH_THRESHOLD = 2;

export function DiscoverPage() {
  const [results, setResults] = useState<DiscoverResult[]>([]);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [pendingDirection, setPendingDirection] = useState<SwipeDirection | null>(null);
  const [match, setMatch] = useState<DiscoverResult | null>(null);
  const fetchingRef = useRef(false);

  const loadPage = useCallback(async (targetPage: number) => {
    if (fetchingRef.current) return;
    fetchingRef.current = true;
    try {
      const response = await getDiscoverResults(DEFAULT_RADIUS_KM, targetPage);
      for (const result of response.results) cachePrompts(result.userId, result.prompts);
      setResults((prev) => (targetPage === 0 ? response.results : [...prev, ...response.results]));
      setHasMore(response.hasMore);
      setPage(targetPage);
    } catch (err) {
      setError(err instanceof ApiError ? err.message : 'Failed to load discovery results.');
    } finally {
      fetchingRef.current = false;
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPage(0);
  }, [loadPage]);

  useEffect(() => {
    if (results.length <= PREFETCH_THRESHOLD && hasMore && !loading) {
      void loadPage(page + 1);
    }
  }, [results.length, hasMore, loading, page, loadPage]);

  function handleSwiped(direction: SwipeDirection) {
    const swipedUser = results[0];
    setResults((prev) => prev.slice(1));
    setPendingDirection(null);
    if (!swipedUser) return;
    swipe(swipedUser.userId, direction)
      .then((response) => {
        if (response.matched) setMatch(swipedUser);
      })
      .catch(() => {
        // Best-effort: the card has already left the stack; a failed swipe
        // write isn't worth blocking or reverting the gesture the user just made.
      });
  }

  const visible = results.slice(0, 3);
  const canAct = visible.length > 0 && pendingDirection === null;

  return (
    <section className="page discover-page">
      <h1>Discover</h1>
      {error && <p className="form__error" role="alert">{error}</p>}

      <div className="swipe-stack">
        {visible.length === 0 && !loading && !error && (
          <p className="page__todo">No one new nearby right now. Check back later.</p>
        )}
        {visible.map((result, index) => (
          <SwipeCard
            key={result.userId}
            result={result}
            isTop={index === 0}
            depth={index}
            forceDirection={index === 0 ? pendingDirection : null}
            onSwiped={handleSwiped}
          />
        ))}
      </div>

      <div className="swipe-actions">
        <button
          type="button"
          className="swipe-actions__button swipe-actions__button--pass"
          aria-label="Pass"
          disabled={!canAct}
          onClick={() => setPendingDirection('PASS')}
        >
          ✕
        </button>
        <button
          type="button"
          className="swipe-actions__button swipe-actions__button--like"
          aria-label="Like"
          disabled={!canAct}
          onClick={() => setPendingDirection('LIKE')}
        >
          ♥
        </button>
      </div>

      {match && <MatchCelebration otherUser={match} onDismiss={() => setMatch(null)} />}
    </section>
  );
}
