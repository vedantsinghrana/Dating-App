import { animate, motion, useMotionValue, useTransform, type PanInfo } from 'framer-motion';
import { useEffect } from 'react';
import type { DiscoverResult, SwipeDirection } from '../../api/types';

const SWIPE_THRESHOLD = 120;
const VELOCITY_THRESHOLD = 500;
const FLY_OUT_X = 700;

interface SwipeCardProps {
  result: DiscoverResult;
  isTop: boolean;
  depth: number;
  forceDirection: SwipeDirection | null;
  onSwiped: (direction: SwipeDirection) => void;
}

export function SwipeCard({ result, isTop, depth, forceDirection, onSwiped }: SwipeCardProps) {
  const x = useMotionValue(0);
  const rotate = useTransform(x, [-300, 300], [-18, 18]);
  const likeOpacity = useTransform(x, [20, SWIPE_THRESHOLD], [0, 1]);
  const passOpacity = useTransform(x, [-SWIPE_THRESHOLD, -20], [1, 0]);

  useEffect(() => {
    if (!forceDirection) return;
    const flyX = forceDirection === 'LIKE' ? FLY_OUT_X : -FLY_OUT_X;
    void animate(x, flyX, { duration: 0.3, ease: 'easeOut' });
    const timeout = setTimeout(() => onSwiped(forceDirection), 220);
    return () => clearTimeout(timeout);
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [forceDirection]);

  function handleDragEnd(_: unknown, info: PanInfo) {
    const pastThreshold =
      Math.abs(info.offset.x) > SWIPE_THRESHOLD || Math.abs(info.velocity.x) > VELOCITY_THRESHOLD;

    if (!pastThreshold) {
      void animate(x, 0, { type: 'spring', stiffness: 300, damping: 25 });
      return;
    }

    const direction: SwipeDirection = info.offset.x > 0 ? 'LIKE' : 'PASS';
    const flyX = direction === 'LIKE' ? FLY_OUT_X : -FLY_OUT_X;
    void animate(x, flyX, { duration: 0.3, ease: 'easeOut' });
    setTimeout(() => onSwiped(direction), 220);
  }

  return (
    <motion.div
      className="swipe-card"
      style={{ x, rotate, zIndex: 10 - depth }}
      drag={isTop ? 'x' : false}
      dragConstraints={{ left: 0, right: 0 }}
      dragElastic={1}
      onDragEnd={isTop ? handleDragEnd : undefined}
      initial={false}
      animate={{ scale: 1 - depth * 0.04, y: depth * 12 }}
      transition={{ type: 'spring', stiffness: 300, damping: 30 }}
    >
      <div
        className="swipe-card__photo"
        style={{ backgroundImage: `url(${result.photos[0]})` }}
      >
        {isTop && (
          <>
            <motion.span className="swipe-card__badge swipe-card__badge--like" style={{ opacity: likeOpacity }}>
              LIKE
            </motion.span>
            <motion.span className="swipe-card__badge swipe-card__badge--pass" style={{ opacity: passOpacity }}>
              PASS
            </motion.span>
          </>
        )}
        <div className="swipe-card__gradient" />
        <div className="swipe-card__info">
          <h2>
            {result.name}, {result.age}
          </h2>
          <p>{result.distanceKm} km away</p>
        </div>
      </div>
      {result.prompts[0] && (
        <div className="swipe-card__prompt">
          <p className="swipe-card__prompt-question">{result.prompts[0].question}</p>
          <p className="swipe-card__prompt-answer">{result.prompts[0].answer}</p>
        </div>
      )}
    </motion.div>
  );
}
