import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import App from '../../App';

describe('LoginPage', () => {
  it('logs in and lands on the discover tab', async () => {
    const user = userEvent.setup();
    render(
      <MemoryRouter initialEntries={['/login']}>
        <App />
      </MemoryRouter>,
    );

    await user.type(screen.getByLabelText(/email/i), 'alex@example.com');
    await user.type(screen.getByLabelText(/password/i), 'hunter2222');
    await user.click(screen.getByRole('button', { name: /log in/i }));

    expect(await screen.findByRole('heading', { name: /discover/i })).toBeInTheDocument();
  });
});
