import type { DiscoverResult } from '../../api/types';

interface DiscoverGridProps {
  results: DiscoverResult[];
  onSelect: (userId: string) => void;
}

// Alternate browse layout over the same GET /discover results the swipe
// stack uses. Picking a tile brings that person to the front of the stack
// and switches back to Stack view so the like/pass gesture still applies.
export function DiscoverGrid({ results, onSelect }: DiscoverGridProps) {
  return (
    <div className="discover-grid">
      {results.map((result) => (
        <button
          key={result.userId}
          type="button"
          className="discover-grid__item"
          onClick={() => onSelect(result.userId)}
        >
          <div
            className="discover-grid__photo"
            style={{ backgroundImage: `url(${result.photos[0]})` }}
          />
          <div className="discover-grid__gradient" />
          <span className="discover-grid__label">
            {result.name}, {result.age}
          </span>
        </button>
      ))}
    </div>
  );
}
