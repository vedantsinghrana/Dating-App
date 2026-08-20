import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it } from 'vitest';
import { setSession } from '../../api/session';
import App from '../../App';

describe('ProfilePage', () => {
  beforeEach(() => {
    setSession({ token: 'mock-jwt-token', userId: '11111111-1111-1111-1111-111111111111' });
  });

  it('loads the current profile and lets you save it', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/profile']}>
        <App />
      </MemoryRouter>,
    );

    const nameInput = await screen.findByDisplayValue('Alex');
    await user.clear(nameInput);
    await user.type(nameInput, 'Alexa');

    await user.click(screen.getByRole('button', { name: /save profile/i }));

    expect(await screen.findByRole('button', { name: /saved/i })).toBeInTheDocument();
  });
});
