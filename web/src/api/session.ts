// In-memory auth session store — deliberately NOT localStorage/sessionStorage.
// A JWT sitting in web storage is readable by any script on the page (XSS
// blast radius); keeping it in memory only means a full page reload clears
// it and the user has to log in again.
//
// NOTE for the backend side: API_CONTRACT.md currently has no refresh/verify
// endpoint, so there's no real "refresh-on-load" to wire up yet — a reload
// always drops the session. `restoreSession` below is the seam for that:
// once a refresh (or "whoami") endpoint exists in the contract, implement it
// there and call it once from main.tsx before the router mounts. Flagged to
// the backend dev as a proposed contract addition rather than assumed.

import { useSyncExternalStore } from 'react';

interface Session {
  token: string;
  userId: string;
}

let session: Session | null = null;
const listeners = new Set<() => void>();

function notify() {
  for (const listener of listeners) listener();
}

export function getSession(): Session | null {
  return session;
}

export function setSession(next: Session) {
  session = next;
  notify();
}

export function clearSession() {
  session = null;
  notify();
}

export async function restoreSession(): Promise<void> {
  // No-op until the API contract exposes a refresh/verify endpoint.
}

function subscribe(listener: () => void) {
  listeners.add(listener);
  return () => listeners.delete(listener);
}

export function useSession(): Session | null {
  return useSyncExternalStore(subscribe, getSession);
}
