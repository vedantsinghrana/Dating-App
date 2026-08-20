Base URL (local dev): http://localhost:8080/api
Auth header: Authorization: Bearer <jwt> on every endpoint except /auth/register and /auth/login.

## Error shape (all endpoints)
json
{ "timestamp": "2026-08-19T10:00:00Z", "status": 400, "error": "Bad Request", "message": "Email already in use", "path": "/api/auth/register" }


## Auth
POST /auth/register — body { "email": string, "password": string } → 201 { "token": string, "userId": string }
POST /auth/login — body { "email": string, "password": string } → 200 { "token": string, "userId": string }

## Profile
GET /profiles/me → 200
json
{
  "userId": "uuid",
  "name": "string",
  "birthdate": "1998-04-12",
  "bio": "string",
  "photos": ["url1", "url2"],
  "prompts": [{ "id": "uuid", "question": "string", "answer": "string" }],
  "location": { "lat": 12.97, "lng": 77.59 },
  "searchRadiusKm": 25
}

PUT /profiles/me — same shape as body (minus userId) → 200 updated profile
POST /profiles/me/photos — multipart file upload → 200 { "url": "string" }
PUT /profiles/me/location — body { "lat": number, "lng": number } → 204

## Discovery (this is the "local matching" endpoint)
GET /discover?radiusKm=25&page=0 → 200
json
{ "results": [ { "userId": "uuid", "name": "string", "age": 27, "photos": ["url"], "prompts": [...], "distanceKm": 4.2 } ], "hasMore": true }

Excludes: self, already-swiped users, existing matches. Sorted by distance ascending.

## Swipes
POST /swipes — body { "toUserId": "uuid", "direction": "LIKE" | "PASS" } → 200 { "matched": boolean, "matchId": "uuid | null" }

## Matches
GET /matches → 200
json
{ "matches": [ { "matchId": "uuid", "otherUser": { "userId": "uuid", "name": "string", "photos": ["url"] }, "createdAt": "iso", "expiresAt": "iso", "openingMoveDone": false } ] }

Business rule: a match expires 48h after creation if no message has been sent (backend enforces via scheduled job; frontend just displays expiresAt and hides/greys out expired matches).

## Messages
GET /matches/{matchId}/messages → 200 { "messages": [ { "id": "uuid", "senderId": "uuid", "content": "string", "sentAt": "iso" } ] }
POST /matches/{matchId}/messages — body { "content": "string", "promptId": "uuid | null" } → 201 message object
Business rule: the FIRST message in a match must include promptId referencing one of the other user's prompts ("opening move"). Backend returns 400 if omitted. Frontend UI should make this the natural flow (tap a prompt to reply to it) rather than a free-text box, so the rule never feels like an error state.

## Realtime chat
WebSocket endpoint: /ws (STOMP over SockJS)
Subscribe: /topic/matches/{matchId}
Send: /app/chat.send — body { "matchId": "uuid", "content": "string" }
Server broadcasts the persisted message (same shape as the REST message object) to /topic/matches/{matchId} on send.

## Daily Top Pick
GET /discover/top-pick → 200 — same shape as one discovery result, or 204 if none available today. Backend computes this once daily per user (scheduled job) via a weighted score: shared prompt/interest overlap + distance + recent activity.
