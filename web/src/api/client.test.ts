import { describe, expect, it } from 'vitest';
import { api } from './client';
import type { AuthResponse } from './types';

describe('api client', () => {
  it('hits the mock login endpoint and returns a token', async () => {
    const result = await api.post<AuthResponse>('/auth/login', {
      email: 'test@example.com',
      password: 'hunter2',
    });

    expect(result.token).toBeTruthy();
    expect(result.userId).toBeTruthy();
  });
});
