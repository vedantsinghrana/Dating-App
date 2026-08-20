import { api } from '../../api/client';
import type { MatchesResponse } from '../../api/types';

export function getMatches() {
  return api.get<MatchesResponse>('/matches');
}
