// Mirrors API_CONTRACT.md exactly. Keep field names/shapes in lockstep with
// that file — if the contract changes, these types change in the same PR.

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}

export interface AuthCredentials {
  email: string;
  password: string;
}

export interface AuthResponse {
  token: string;
  userId: string;
}

export interface Prompt {
  id: string;
  question: string;
  answer: string;
}

export interface GeoPoint {
  lat: number;
  lng: number;
}

export interface Profile {
  userId: string;
  name: string;
  birthdate: string;
  bio: string;
  photos: string[];
  prompts: Prompt[];
  location: GeoPoint;
  searchRadiusKm: number;
}

export type ProfileUpdate = Omit<Profile, 'userId'>;

export interface PhotoUploadResponse {
  url: string;
}

export interface DiscoverResult {
  userId: string;
  name: string;
  age: number;
  photos: string[];
  prompts: Prompt[];
  distanceKm: number;
}

export interface DiscoverResponse {
  results: DiscoverResult[];
  hasMore: boolean;
}

export type SwipeDirection = 'LIKE' | 'PASS';

export interface SwipeRequest {
  toUserId: string;
  direction: SwipeDirection;
}

export interface SwipeResponse {
  matched: boolean;
  matchId: string | null;
}

export interface MatchOtherUser {
  userId: string;
  name: string;
  photos: string[];
}

export interface Match {
  matchId: string;
  otherUser: MatchOtherUser;
  createdAt: string;
  expiresAt: string;
  openingMoveDone: boolean;
}

export interface MatchesResponse {
  matches: Match[];
}

export interface Message {
  id: string;
  senderId: string;
  content: string;
  sentAt: string;
}

export interface MessagesResponse {
  messages: Message[];
}

export interface SendMessageRequest {
  content: string;
  promptId: string | null;
}
