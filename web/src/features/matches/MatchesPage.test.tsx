import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { cachePrompts } from '../../api/promptCache';
import { setSession } from '../../api/session';
import App from '../../App';

describe('MatchesPage', () => {
  beforeEach(() => {
    setSession({ token: 'mock-jwt-token', userId: '11111111-1111-1111-1111-111111111111' });
    sessionStorage.clear();
  });

  it('requires picking a prompt before the reply box appears, then opens chat', async () => {
    // Simulate having seen Sam (discover-2, match-2's otherUser) on Discover already.
    cachePrompts('discover-2', [
      { id: 'prompt-3-0', question: 'A perfect Sunday looks like', answer: 'Coffee and cacti' },
    ]);

    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/matches']}>
        <App />
      </MemoryRouter>,
    );

    const samRow = await screen.findByText('Sam');
    await user.click(samRow);

    expect(screen.queryByPlaceholderText(/say something/i)).not.toBeInTheDocument();

    await user.click(screen.getByText(/coffee and cacti/i));
    const replyBox = screen.getByPlaceholderText(/say something/i);
    await user.type(replyBox, 'Team cacti forever');
    await user.click(screen.getByRole('button', { name: /^send$/i }));

    expect(await screen.findByRole('heading', { name: /chat/i })).toBeInTheDocument();
  });
});
