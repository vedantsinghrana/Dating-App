import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import { describe, expect, it } from 'vitest';
import App from './App';

describe('App', () => {
  it('redirects an unauthenticated visitor to /login', () => {
    render(
      <MemoryRouter initialEntries={['/discover']}>
        <App />
      </MemoryRouter>,
    );

    expect(screen.getByRole('heading', { name: /log in/i })).toBeInTheDocument();
  });
});
