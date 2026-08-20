# web — Dating App frontend

React + Vite + TypeScript client. See `../API_CONTRACT.md` at the repo root for the
endpoint contract this app builds against, and `../WORKFLOW_AND_ROADMAP.md` for
repo-wide conventions.

## Getting started

```bash
npm install
cp .env.example .env
npm run dev
```

By default `.env.example` sets `VITE_USE_MOCKS=true`, so the app boots against an
in-browser mock server (MSW) seeded from `API_CONTRACT.md`'s example payloads —
no backend required. To point at a real backend instead, set `VITE_USE_MOCKS=false`
and `VITE_API_BASE_URL` to where it's running; no code changes needed either way.

## Scripts

| Command | What it does |
|---|---|
| `npm run dev` | Start the Vite dev server |
| `npm run lint` | oxlint |
| `npm run typecheck` | `tsc -b --noEmit` |
| `npm run test` | Vitest (jsdom + Testing Library + MSW node server) |
| `npm run build` | Type-check then production build |

## Structure

```
src/
├── api/            # fetch client, session (in-memory JWT), types, chat WebSocket, MSW mocks
├── theme/           # design tokens (light/dark via CSS variables) + ThemeProvider
├── routes/           # router shell: AppLayout (tab bar), RequireAuth
├── components/      # shared/dumb UI components
└── features/
    ├── auth/         # login/register
    ├── profile/      # view/edit profile, photos, prompts
    ├── discovery/    # swipe stack + grid view
    ├── matches/      # match list + opening-move flow
    └── chat/         # live chat over STOMP/SockJS
```

Auth token is kept in memory only (not localStorage) to limit XSS blast radius —
see the comment in `src/api/session.ts` for the tradeoff (a full page reload
currently drops the session; there's no refresh/verify endpoint in
`API_CONTRACT.md` yet to restore it from).
