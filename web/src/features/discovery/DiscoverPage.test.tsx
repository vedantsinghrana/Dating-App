import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { setSession } from '../../api/session';
import App from '../../App';

describe('DiscoverPage', () => {
  beforeEach(() => {
    setSession({ token: 'mock-jwt-token', userId: '11111111-1111-1111-1111-111111111111' });
    // Force the mock /swipes handler's random match roll to miss, so this
    // test deterministically advances to the next card instead of showing
    // the match celebration overlay.
    vi.spyOn(Math, 'random').mockReturnValue(0.9);
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('advances to the next card after liking the top one', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/discover']}>
        <App />
      </MemoryRouter>,
    );

    expect(await screen.findByText(/priya/i)).toBeInTheDocument();

    await user.click(screen.getByRole('button', { name: /like/i }));

    await waitFor(() => expect(screen.queryByText(/priya/i)).not.toBeInTheDocument(), {
      timeout: 2000,
    });
    expect(screen.getByText(/jordan/i)).toBeInTheDocument();
  });

  it('switches to grid view and picking a tile brings it to the front of the stack', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/discover']}>
        <App />
      </MemoryRouter>,
    );

    await screen.findByText(/priya/i);
    await user.click(screen.getByRole('radio', { name: /grid/i }));

    const samTile = await screen.findByRole('button', { name: /sam, \d+/i });
    await user.click(samTile);

    expect(await screen.findByRole('radio', { name: /stack/i })).toHaveAttribute(
      'aria-checked',
      'true',
    );
    expect(screen.getByText(/sam/i)).toBeInTheDocument();
  });
});
