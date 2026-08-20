import { http, HttpResponse } from 'msw';
import type {
  AuthResponse,
  DiscoverResponse,
  MatchesResponse,
  MessagesResponse,
  PhotoUploadResponse,
  Profile,
  SwipeRequest,
  SwipeResponse,
} from '../types';
import {
  MOCK_USER_ID,
  mockDiscoverResults,
  mockMatches,
  mockMessagesByMatch,
  mockProfile,
} from './data';

const BASE = (import.meta.env.VITE_API_BASE_URL as string) || 'http://localhost:8080/api';

let profile: Profile = { ...mockProfile };

export const handlers = [
  http.post(`${BASE}/auth/register`, async () => {
    return HttpResponse.json<AuthResponse>(
      { token: 'mock-jwt-token', userId: MOCK_USER_ID },
      { status: 201 },
    );
  }),

  http.post(`${BASE}/auth/login`, async () => {
    return HttpResponse.json<AuthResponse>({ token: 'mock-jwt-token', userId: MOCK_USER_ID });
  }),

  http.get(`${BASE}/profiles/me`, () => {
    return HttpResponse.json<Profile>(profile);
  }),

  http.put(`${BASE}/profiles/me`, async ({ request }) => {
    const body = (await request.json()) as Omit<Profile, 'userId'>;
    profile = { ...profile, ...body };
    return HttpResponse.json<Profile>(profile);
  }),

  http.post(`${BASE}/profiles/me/photos`, () => {
    return HttpResponse.json<PhotoUploadResponse>({
      url: `https://picsum.photos/seed/upload-${Date.now()}/600/800`,
    });
  }),

  http.put(`${BASE}/profiles/me/location`, async ({ request }) => {
    const body = (await request.json()) as { lat: number; lng: number };
    profile = { ...profile, location: body };
    return new HttpResponse(null, { status: 204 });
  }),

  http.get(`${BASE}/discover`, ({ request }) => {
    const url = new URL(request.url);
    const page = Number(url.searchParams.get('page') ?? '0');
    const pageSize = 6;
    const start = page * pageSize;
    const results = mockDiscoverResults.slice(start, start + pageSize);
    return HttpResponse.json<DiscoverResponse>({
      results,
      hasMore: start + pageSize < mockDiscoverResults.length,
    });
  }),

  http.get(`${BASE}/discover/top-pick`, () => {
    const pick = mockDiscoverResults[0];
    return pick ? HttpResponse.json(pick) : new HttpResponse(null, { status: 204 });
  }),

  http.post(`${BASE}/swipes`, async ({ request }) => {
    const body = (await request.json()) as SwipeRequest;
    const matched = body.direction === 'LIKE' && Math.random() < 0.35;
    return HttpResponse.json<SwipeResponse>({
      matched,
      matchId: matched ? `match-${body.toUserId}` : null,
    });
  }),

  http.get(`${BASE}/matches`, () => {
    return HttpResponse.json<MatchesResponse>({ matches: mockMatches });
  }),

  http.get(`${BASE}/matches/:matchId/messages`, ({ params }) => {
    const { matchId } = params as { matchId: string };
    return HttpResponse.json<MessagesResponse>({
      messages: mockMessagesByMatch[matchId] ?? [],
    });
  }),

  http.post(`${BASE}/matches/:matchId/messages`, async ({ params, request }) => {
    const { matchId } = params as { matchId: string };
    const body = (await request.json()) as { content: string; promptId: string | null };
    const match = mockMatches.find((m) => m.matchId === matchId);

    const history = mockMessagesByMatch[matchId] ?? [];
    const isFirstMessage = history.length === 0;
    if (isFirstMessage && !body.promptId) {
      return HttpResponse.json(
        {
          timestamp: new Date().toISOString(),
          status: 400,
          error: 'Bad Request',
          message: 'First message in a match must reference a promptId (opening move).',
          path: `/matches/${matchId}/messages`,
        },
        { status: 400 },
      );
    }

    const message = {
      id: `msg-${Date.now()}`,
      senderId: MOCK_USER_ID,
      content: body.content,
      sentAt: new Date().toISOString(),
    };
    mockMessagesByMatch[matchId] = [...history, message];
    if (match) match.openingMoveDone = true;

    return HttpResponse.json(message, { status: 201 });
  }),
];
