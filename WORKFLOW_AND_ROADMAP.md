# Workflow, Structure & Roadmap

Companion to `PROJECT_PLAN.md` (stack/features/UI) and `API_CONTRACT.md` (endpoint spec). This document covers how the repo is organized, how the two of you actually work day-to-day without colliding, and the full roadmap from setup through post-MVP scope.

---

## 1. Repository Structure

One monorepo, one repo owned by your brother, you as collaborator. Folder-per-surface is what makes parallel work safe — you and your brother essentially never edit the same file.

```
dating-app/
├── README.md
├── PROJECT_PLAN.md
├── API_CONTRACT.md
├── WORKFLOW_AND_ROADMAP.md
├── docker-compose.yml            # postgres+postgis service; edited only to add/adjust services
├── .github/
│   └── workflows/
│       ├── backend-ci.yml        # triggers only on backend/** changes
│       └── frontend-ci.yml       # triggers only on web/** changes
├── backend/
│   ├── src/main/java/com/app/dating/
│   │   ├── auth/
│   │   ├── profile/
│   │   ├── discovery/            # geo search
│   │   ├── matching/             # swipes, matches, expiry job
│   │   ├── chat/                 # REST + WebSocket
│   │   └── common/               # security config, exception handling, shared utils
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/migration/         # Flyway SQL migrations — real schema history
│   ├── src/test/java/...
│   ├── build.gradle
│   ├── Dockerfile
│   └── .env.example
└── web/
    ├── src/
    │   ├── api/                  # API client + MSW mocks, one place that knows about HTTP
    │   ├── features/             # auth/, profile/, discovery/, matches/, chat/ — one folder per domain
    │   ├── components/           # shared/dumb UI components
    │   ├── theme/                # design tokens, light/dark mode
    │   └── main.tsx
    ├── public/
    ├── package.json
    ├── vite.config.ts
    └── .env.example

# mobile/ gets created in Phase 3, not before — an empty placeholder folder now just invites
# accidental early edits and merge noise for a client that doesn't exist yet.
```

Within each of `backend/` and `web/`, organize by **feature/domain**, not by technical layer (i.e. `profile/{Controller,Service,Repository}` together, not one giant `controllers/` folder for everything). This keeps each person's PRs naturally scoped to one folder even as the app grows, and it reads better in a portfolio review.

---

## 2. Git & Day-to-Day Workflow

**Branching:** `main` is protected — no direct pushes, PR + passing CI + one approval (from the other of you) required to merge. Feature branches: `backend/<feature>`, `frontend/<feature>`, `chore/<thing>` for cross-cutting housekeeping (README updates, CI config, etc.).

**Commits:** Conventional Commits — `feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `chore:`. Small, frequent commits over one giant commit per feature; it makes review and history actually useful.

**The loop each of you follows every session:**
1. `git checkout main && git pull`
2. `git checkout -b backend/<feature>` (or `frontend/<feature>`)
3. Open Claude Code, use your assigned prompt (from `PROMPT_BACKEND.md` / `PROMPT_FRONTEND.md`), scoped to your folder only
4. Build, test locally
5. Commit in small chunks, push
6. Open a PR against `main`, tag the other person as reviewer
7. Address feedback → CI green → squash-merge

**CI gates (required to merge):** backend — build + unit tests + integration tests; frontend — lint + type-check + unit tests + build. Path-scoped workflows mean a frontend-only PR doesn't wait on backend tests and vice versa.

**Config/secrets:** `.env.example` committed in each of `backend/` and `web/` listing required variables (`DB_URL`, `JWT_SECRET`, `VITE_API_BASE_URL`, `VITE_USE_MOCKS`, etc.) with placeholder values; real `.env` files are gitignored, never committed.

**Keeping the contract in sync:** if either of you needs a change to `API_CONTRACT.md`, open a tiny standalone PR touching only that file, get the other person to approve it, merge it first, *then* both pull and continue your feature branches against the updated contract. Never let the contract drift silently out of sync with either implementation.

**Deployment:** keep it simple for a two-person project — no elaborate staging/prod split. `docker-compose up` for local dev; one deployed demo environment (backend + Postgres on Railway/Render, web on Vercel), redeployed on merge to `main`. A live link plus a green CI badge in the README is the actual resume payoff, so don't over-invest in infra beyond that.

---

## 3. Roadmap

### Phase 0 — Setup
Repo scaffold as above, Docker Compose (Postgres + PostGIS), empty Spring Boot + Vite skeletons deployed end-to-end as a thin vertical slice (a health-check endpoint the web app can call), CI wired up, `API_CONTRACT.md` and `.env.example` in place. Do this together in one sitting so you both start from the same base.

### Phase 1 — Core MVP
Auth, profile with photos + prompts, PostGIS-based local discovery, swipe → match, real-time chat, required "opening move" on first message. This is what `PROMPT_BACKEND.md` / `PROMPT_FRONTEND.md` are scoped to build. Exit criteria: two real accounts can register, appear in each other's discovery feed, match, and chat live, deployed and reachable by URL.

### Phase 2 — Engagement layer
Daily Top Pick (weighted-scoring recommendation), 48h match expiry job, Boost, Grid/browse view toggle, dark mode polish. These were deliberately deferred out of Phase 1 so the core loop ships first and stays demoable throughout — each of these is a self-contained addition on top of a working app rather than a dependency of it.

### Phase 3 — Mobile
Android client (Kotlin) added as `/mobile`, consuming the same `API_CONTRACT.md` — no backend changes needed if the contract was followed strictly in Phase 1. This is also where a real object-storage swap (see Phase 4) starts to matter, since a mobile client makes "local file storage on the backend server" clearly not viable long-term.

### Phase 4 — Production-readiness (future scope, not resume-blocking but shows maturity if you get here)
- Real object storage for photos (S3-compatible: AWS S3 or Cloudflare R2) replacing local disk storage
- Safety features: block/report user, basic message/bio content filtering
- Rate limiting & abuse prevention (swipe-rate caps, login throttling)
- Push notifications (FCM) for new matches/messages
- Observability: structured logging, metrics (Micrometer), error tracking (Sentry)
- If ever running multiple backend instances: externalize WebSocket session/pub-sub state (Redis) since STOMP sessions are sticky to one instance by default

### Phase 5 — Growth / stretch scope
- Upgrade Top Pick from hand-weighted scoring to embeddings-based similarity matching — a natural, explainable evolution of the Phase 2 feature, good "here's how I'd scale this" interview answer even if you only design it and don't fully build it
- Video chat (WebRTC)
- Premium tier / payments (Stripe) gating Boost, "see who liked you," etc.
- Social login (Google/Apple OAuth2)
- iOS client
- Admin dashboard for basic moderation/analytics
- Internationalization

**Explicit non-goals for this project:** building custom ML infrastructure, handling real payment compliance (PCI etc.) beyond a Stripe test-mode integration, and large-scale infra (multi-region, sharding) — none of that is necessary to make this a strong resume piece, and reaching for it prematurely is more likely to leave you with an unfinished app than an impressive one. The value of Phases 4–5 is mostly in being able to *articulate* the extension path in an interview, not in shipping all of it.

---

## 4. Why this structure holds up as the app grows

Because `API_CONTRACT.md` is versioned in the repo and both sides build against it, adding a Phase 2+ endpoint is additive (new fields/endpoints) rather than a breaking renegotiation — bump a `v2` prefix only if you ever need an actually incompatible change. Because the backend and frontend are organized by domain (`profile/`, `discovery/`, `matching/`, `chat/`) rather than by technical layer, adding the Android client in Phase 3 or the moderation features in Phase 4 means adding new folders, not restructuring existing ones. And because CI is path-scoped and PRs are folder-scoped by convention, the two-person workflow that works for Phase 1 keeps working without renegotiation as the app grows.
