import { motion } from 'framer-motion';
import { useNavigate } from 'react-router-dom';
import type { DiscoverResult } from '../../api/types';

interface MatchCelebrationProps {
  otherUser: DiscoverResult;
  onDismiss: () => void;
}

export function MatchCelebration({ otherUser, onDismiss }: MatchCelebrationProps) {
  const navigate = useNavigate();

  return (
    <motion.div
      className="match-celebration"
      initial={{ opacity: 0 }}
      animate={{ opacity: 1 }}
      exit={{ opacity: 0 }}
    >
      <motion.div
        className="match-celebration__card"
        initial={{ scale: 0.7, opacity: 0 }}
        animate={{ scale: 1, opacity: 1 }}
        transition={{ type: 'spring', stiffness: 260, damping: 20 }}
      >
        <p className="match-celebration__eyebrow">It's a match!</p>
        <div
          className="match-celebration__photo"
          style={{ backgroundImage: `url(${otherUser.photos[0]})` }}
        />
        <h2>You and {otherUser.name} liked each other</h2>
        <div className="match-celebration__actions">
          <button type="button" className="button button--secondary" onClick={onDismiss}>
            Keep swiping
          </button>
          <button
            type="button"
            className="button button--primary"
            onClick={() => navigate('/matches')}
          >
            Say hi
          </button>
        </div>
      </motion.div>
    </motion.div>
  );
}
