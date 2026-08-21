// sessionStorage-backed cache of prompts seen while browsing Discover, keyed
// by userId. API_CONTRACT.md's Match/otherUser shape only carries
// { userId, name, photos } — no prompts — but the opening-move flow needs
// the other person's prompts to let the user pick one to reply to. This is a
// stopgap populated from DiscoverResult as cards are shown; matches from
// outside this browser tab/session won't have cached prompts. Flagged to the
// backend dev as a proposed contract addition (e.g. prompts on
// MatchOtherUser, or a GET /profiles/{userId}) rather than assumed.

import type { Prompt } from './types';

const KEY = 'dating-app-prompt-cache';

function readCache(): Record<string, Prompt[]> {
  try {
    return JSON.parse(sessionStorage.getItem(KEY) ?? '{}') as Record<string, Prompt[]>;
  } catch {
    return {};
  }
}

export function cachePrompts(userId: string, prompts: Prompt[]) {
  const cache = readCache();
  cache[userId] = prompts;
  sessionStorage.setItem(KEY, JSON.stringify(cache));
}

export function getCachedPrompts(userId: string): Prompt[] | null {
  return readCache()[userId] ?? null;
}
