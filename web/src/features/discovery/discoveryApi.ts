import { api } from '../../api/client';
import type { DiscoverResponse, SwipeDirection, SwipeResponse } from '../../api/types';

export const DEFAULT_RADIUS_KM = 25;

export function getDiscoverResults(radiusKm: number, page: number) {
  return api.get<DiscoverResponse>(`/discover?radiusKm=${radiusKm}&page=${page}`);
}

export function swipe(toUserId: string, direction: SwipeDirection) {
  return api.post<SwipeResponse>('/swipes', { toUserId, direction });
}
