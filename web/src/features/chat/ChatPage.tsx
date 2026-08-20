import { useParams } from 'react-router-dom';

export function ChatPage() {
  const { matchId } = useParams<{ matchId: string }>();
  return (
    <section className="page">
      <h1>Chat</h1>
      <p className="page__todo">TODO (Phase 1 step 6): live WebSocket chat for match {matchId}.</p>
    </section>
  );
}
