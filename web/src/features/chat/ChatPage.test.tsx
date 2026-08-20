import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { setSession } from '../../api/session';
import App from '../../App';

describe('ChatPage', () => {
  beforeEach(() => {
    setSession({ token: 'mock-jwt-token', userId: '11111111-1111-1111-1111-111111111111' });
  });

  it('loads REST history and lets you send a new message', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/chat/match-1']}>
        <App />
      </MemoryRouter>,
    );

    expect(await screen.findByText(/hot dog is a sandwich/i)).toBeInTheDocument();

    await user.type(screen.getByLabelText(/message/i), 'Agree to disagree');
    await user.click(screen.getByRole('button', { name: /^send$/i }));

    expect(await screen.findByText('Agree to disagree')).toBeInTheDocument();
  });
});
