# Dating App — Project Plan

A resume-grade, full-stack dating app built collaboratively by two developers: Java/Spring Boot backend, React web client, Android (Kotlin) client to follow. This document covers the stack, competitive research, recommended feature set, UI direction, and build roadmap.

---

## 1. Tech Stack

| Layer | Choice | Why |
|---|---|---|
| Backend | Java 21 + Spring Boot 3 (Spring Web, Spring Data JPA, Spring Security, Spring WebSocket) | Industry-standard Java backend combo, strong resume signal, one language to master |
| Database | PostgreSQL + PostGIS extension | Relational data + native geospatial queries for "nearby matches" |
| Web frontend | React (Vite) | Fast to build, separates concerns cleanly from the API, in-demand skill |
| Mobile (phase 2) | Android, Kotlin | Same REST/WebSocket API as web; Kotlin is the modern Android default |
| Auth | Spring Security + JWT (Google OAuth2 optional add-on) | Standard, well-documented, portfolio-friendly |
| Realtime | Spring WebSocket (STOMP) | Powers live chat |
| Infra | Docker Compose (local), GitHub Actions (CI), Railway/Render (backend deploy), Vercel/Netlify (web deploy) | Free/cheap, gives you a live deployed link + CI badge for the resume |

Architecture stays a single well-structured Spring Boot service (not microservices) — for a two-person team, microservices add operational overhead without teaching you more. Layered internally: `controller → service → repository`, with clear domain boundaries (auth, profiles, matching, chat) so the codebase still *reads* like a properly designed system.

---

## 2. Competitive Research: What the Major Apps Actually Do

### Tinder
Core loop is the swipe card stack — left/right gesture over right/wrong buttons, which turns matching into a game rather than a browsing task. Profiles are photo-forward with minimal text. Standout mechanics: **Super Like** (stronger signal, more visibility), **Boost** (30-minute visibility spike), **Rewind** (undo last swipe), **Passport** (browse other cities), Spotify/Instagram integration for shared-interest signals, proximity-first ordering.

### Bumble
Same swipe core, but profile-building is richer: up to 6 photos, bio, 5 interests, and **prompt responses**, plus a required **opening move** (an icebreaker prompt the other person must answer to start the chat — kills the "hey" problem). Matches **expire after 24 hours** without a message, which creates urgency and keeps the queue moving. Other notable bits: drag-and-drop photo reordering, "New / Nearby / Recently active" filters, and video/voice messages in chat.

### Hinge
Deliberately anti-swipe-spam. Profiles are built around **prompts** (users answer 3+ conversation-starter prompts instead of just a bio), and the only way to express interest is to **comment on a specific photo or prompt answer** — you can't "blind like" a whole profile. Two standout algorithmic features: **Standouts** (a curated daily gallery of high-potential profiles) and **Most Compatible** (one algorithmically-chosen "best match" per day, refined over time by a **"We Met" feedback loop** where users report back on how the date went).

### Grindr
Skips the swipe stack entirely in favor of **The Grid** — a proximity-sorted grid of nearby users, browsable and filterable (Tags, Fresh, Popular), with an **Explore** mode to browse other locations. It's the clearest example of "location" being the primary organizing principle of the UI rather than an algorithm sorting behind the scenes.

### The pattern across all four
Proximity/location is table stakes in every one of these apps — it's not a differentiator, it's a baseline requirement. What actually differentiates them is (a) how much effort it takes to express interest (blind swipe vs. commenting on a specific prompt), (b) whether there's a curated "best pick" surfaced by an algorithm, and (c) small urgency/gamification mechanics (expiring matches, boosts, streaks).

Sources: [SwipeStats Tinder review](https://www.swipestats.io/blog/tinder-review) · [SwipeStats Bumble review](https://www.swipestats.io/blog/bumble-review) · [SwipeStats Hinge review](https://www.swipestats.io/blog/hinge-review) · [Grindr Help Center — The Grid](https://help.grindr.com/hc/en-us/articles/12155355365011-The-Grid) · [RedRocket — Tinder UX breakdown](https://redrocket.software/blog/breaking-down-tinders-user-experience)

---

## 3. Recommended Feature Set (MVP, adapted not copied)

**Core (must-have):**
- Auth: email/password + JWT; Google login as a stretch add-on
- Profile: multiple photos, bio, **3 prompt-style questions** (Hinge-inspired — richer and more interesting to build than a plain bio field)
- **Local/proximity matching**: PostGIS radius query, adjustable search distance, sorted by distance
- Swipe stack as the primary interaction (Tinder-style — fun, familiar, and a great front-end animation showcase)
- Mutual like → Match → real-time chat (WebSocket)
- Required **opening move** on new matches (Bumble-inspired — first message must respond to one of the match's prompts, avoids the "hey" problem and is a nice, easy-to-explain "product thinking" feature for interviews)

**Fun / differentiating features (what makes it feel like a real product, not a CRUD demo):**
- **Daily Top Pick** — a simple scored recommendation ("Most Compatible"-inspired): rank candidate profiles by shared interests + distance + recent activity, surface one per day. No ML needed for v1 — a weighted-scoring function is honest, explainable, and still a great resume bullet ("built a matching/recommendation feature"). Can be upgraded to embeddings-based similarity later as a stretch goal.
- **Match expiry** — unanswered matches expire after 48 hours (Bumble-inspired), implemented as a Spring `@Scheduled` background job — a nice, easy way to demonstrate scheduled/background processing skills.
- **Boost** — a lightweight "boost my profile for 30 minutes" mechanic that temporarily re-ranks you higher in others' stacks.
- **Grid/browse view toggle** (Grindr-inspired) — a second way to view nearby profiles besides the swipe stack, reusing the same geo-query endpoint. Cheap to add once proximity search exists, and shows you can design more than one UI on top of one API.
- **Dark mode toggle** — small, but it's one of the most requested UI features in any consumer app and easy to implement with a design token system, so worth including for UI polish.

**Explicitly out of scope for MVP** (mention as "roadmap" items on the resume, don't build yet): video chat, payments/subscriptions, content moderation ML, push notifications (add once mobile client exists).

---

## 4. UI/UX Direction

Photo-forward, card-based, minimal chrome — follow Tinder/Hinge's lead rather than inventing a new paradigm; recruiters and users both recognize this pattern instantly, which lowers the "why doesn't this feel like a dating app" risk. Concretely:

- Swipe cards with real drag/animation physics (React: `framer-motion` or `react-tinder-card`), not just click buttons — this is the single highest-impact visual/interaction detail for making it feel "real."
- Prompt-based profile cards (photo + prompt answer overlays), not just a photo + name + bio block.
- A warm, single-accent-color visual identity (pick one brand color + neutrals, not a rainbow) with rounded cards and soft shadows — consistent, not flashy.
- Bottom tab navigation on mobile-width layouts (Matches / Discover / Chat / Profile), which also makes the eventual Android port more natural since the IA already matches a mobile app.
- Dark mode via CSS variables / design tokens from day one, so it isn't a bolt-on later.

---

## 5. Data Model Additions for Geo Matching

```
Profile
  ...
  location  geography(Point, 4326)   -- PostGIS point (lng, lat)
  search_radius_km  int

-- nearby query (conceptual)
SELECT p.*, ST_Distance(p.location, :myLocation) AS distance_m
FROM profile p
WHERE ST_DWithin(p.location, :myLocation, :radiusMeters)
ORDER BY distance_m
```

Index `location` with a GiST index for performant radius queries. This is the same approach used in production location-based apps (PostGIS `geography` type + `ST_DWithin`), and it's a genuinely good, explainable interview topic ("how did you implement nearby search").

---

## 6. Build Roadmap

1. Repo scaffold: monorepo (`/backend`, `/web`, `/mobile`), Docker Compose (Postgres + PostGIS), empty Spring Boot skeleton deployed end-to-end (thin vertical slice first — kills integration surprises later)
2. Auth + Profile CRUD (with prompts, photos) + tests
3. Geo matching: location capture, PostGIS radius query, distance-sorted results
4. Web UI: swipe stack, profile cards, prompt display
5. Matching + opening-move flow
6. Real-time chat (WebSocket)
7. Fun features: daily Top Pick, match expiry job, Boost, Grid view toggle, dark mode
8. Deploy, polish README (screenshots/GIF), CI badge
9. Stretch: Android client (Kotlin) against the same API

---

## 7. Resume Framing (once built)

*"Designed and built a full-stack dating app (Java/Spring Boot, PostgreSQL/PostGIS, React) with real-time chat over WebSocket, geospatial proximity matching, and a custom recommendation-scoring feature; deployed with CI/CD via GitHub Actions."*
