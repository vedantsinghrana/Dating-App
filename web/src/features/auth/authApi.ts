import { api } from '../../api/client';
import type { AuthCredentials, AuthResponse } from '../../api/types';

export function register(credentials: AuthCredentials) {
  return api.post<AuthResponse>('/auth/register', credentials);
}

export function login(credentials: AuthCredentials) {
  return api.post<AuthResponse>('/auth/login', credentials);
}
