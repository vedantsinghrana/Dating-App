# Dating App

A full-stack dating app built around **where you actually are**: proximity-based matching on PostGIS, Hinge-style prompt profiles instead of a blank bio box, a required "opening move" so nobody has to read another "hey," and real-time chat over WebSocket. Java/Spring Boot backend, React web client, Android client to follow.

[![backend-ci](https://github.com/vedantsinghrana/Dating-App/actions/workflows/backend-ci.yml/badge.svg)](https://github.com/vedantsinghrana/Dating-App/actions/workflows/backend-ci.yml)
![Java](https://img.shields.io/badge/Java-21-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-brightgreen)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-PostGIS-336791)

---

## Why this app

Most portfolio dating-app clones stop at "swipe and match." This one borrows the mechanic each major app actually got right, and builds one coherent product out of them:

- **Proximity as the organizing principle** (Grindr) — location isn't a filter bolted on after the fact, it's a first-class PostGIS `geography(Point,4326)` column with radius search from day one.
- **Prompt-based profiles, not a bio box** (Hinge) — 3+ prompt/answer pairs per profile, richer and more interesting to build (and to read) than free text.
- **A required opening move** (Bumble) — the first message in a match must respond to one of the other person's prompts, so the conversation starts from something real instead of "hey."
- **Matches that expire** (Bumble) — an unanswered match quietly disappears after 48 hours via a scheduled job, which keeps the queue moving and doubles as a clean demo of background job processing.
- **The swipe stack as the core loop** (Tinder) — familiar, fun, and a genuinely good front-end animation showcase.

## Tech stack

| Layer | Choice | Why |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 (Web, Data JPA, Security, WebSocket) | One language, industry-standard combo, strong resume signal |
| Database | PostgreSQL + PostGIS | Relational data with native geospatial radius queries |
| Web frontend | React (Vite) | Fast iteration, clean separation from the API |
| Mobile *(phase 3)* | Android, Kotlin | Same REST/WebSocket contract as web |
| Auth | Spring Security + JWT | Stateless, standard, well understood |
| Realtime | Spring WebSocket (STOMP) | Powers live chat |
| Infra | Docker Compose (local), GitHub Actions (CI) | Reproducible local dev, CI badge for the resume |

## Repository layout

This is a monorepo, split by surface so backend and frontend work never touch the same files:

```
dating-app/
├── API_CONTRACT.md            # the single source of truth for every endpoint shape
├── PROJECT_PLAN.md            # stack, competitive research, feature rationale
├── WORKFLOW_AND_ROADMAP.md    # how we work day-to-day, and the phase-by-phase roadmap
├── docker-compose.yml         # postgres+postgis service for local dev
├── backend/                   # Spring Boot API — see backend/README below (coming soon)
│   └── src/main/java/com/app/dating/{auth,profile,discovery,matching,chat,common}/
└── web/                       # React client (in progress)
```

## Getting started (backend)

```bash
git clone https://github.com/vedantsinghrana/Dating-App.git
cd Dating-App

# start Postgres + PostGIS
docker compose up -d postgres

# run the API
cd backend
./gradlew bootRun
```

Then confirm it's alive:

```bash
curl http://localhost:8080/api/health
# {"status":"UP"}
```

Run the test suite with `./gradlew test`.

## API contract

Every endpoint — request/response shapes, error format, auth header, the WebSocket chat protocol — is specified in [`API_CONTRACT.md`](API_CONTRACT.md). Backend and frontend are both built against it, so it's the thing to read before touching either side.

## Current status

**Backend**
- [x] Project skeleton, Docker Compose Postgres/PostGIS, CI, health check
- [x] `User` / `Profile` entities and Flyway migrations (photos and prompts as related tables)
- [x] Auth — `/api/auth/register`, `/api/auth/login`, JWT issuance, Spring Security filter chain
- [ ] Profile endpoints (`GET/PUT /api/profiles/me`, photo upload, location update)
- [ ] Discovery (`GET /api/discover`, PostGIS radius search, pagination)
- [ ] Swipes, match creation, 48h match-expiry job
- [ ] Messages — REST history + WebSocket live chat, opening-move rule

**Web** — not started yet.
**Mobile** — planned for a later phase, once the web client and API have stabilized.

## Roadmap & workflow

The phase-by-phase build order and the day-to-day git workflow (branch naming, commit style, PR process) live in [`WORKFLOW_AND_ROADMAP.md`](WORKFLOW_AND_ROADMAP.md). The product/design rationale — including the competitive research behind each feature choice — lives in [`PROJECT_PLAN.md`](PROJECT_PLAN.md).
