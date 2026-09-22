# PlanTogether

A collaborative trip-planning app. Create a trip, invite friends by link, propose destinations and date ranges, vote on them, and build the itinerary together live, so the plan doesn't get lost in a group chat.

> **Status:** early development. Phase 1 is in progress: the skeleton, the health check and CI are done, and email-OTP authentication is being built. Active work happens on `dev`; `main` receives finished milestones.

---

## What it will do

- **Trips.** Create a trip, list your trips, and open a trip's details.
- **Invites and membership.** Invite people with a link, join with a link, list members, leave or remove someone.
- **Proposals and voting.** Anyone can propose a destination with a date range. Votes close at a deadline, and results are shown once voting has closed.
- **Itinerary.** Days and items that several people can edit and reorder at the same time.
- **Map.** A Leaflet view of the itinerary.
- **Email notifications.** Sent when you're invited and when a vote closes.

## Tech stack

| Area | Choice |
|---|---|
| Backend | Java 21, Spring Boot 4, Spring Security, `JdbcClient`, Flyway |
| Database | PostgreSQL 16 |
| Frontend | React, Vite, TypeScript, Leaflet |
| Testing | JUnit 5, MockMvc, Testcontainers, Playwright; Vitest + MSW (planned) |
| Infrastructure | Docker Compose, GitHub Actions |
| Later phases | WebSocket (STOMP), Redis pub/sub, RabbitMQ, Prometheus/Grafana |

---

## Roadmap

The build is **phased on purpose**. Phase 1 is a Postgres-only monolith. Each later phase adds one piece of infrastructure, on top of something that already works.

| Phase | Adds |
|---|---|
| **1** | Trips, invites, membership, proposals and voting, itinerary CRUD, auth, CI |
| **2** | Live collaboration: WebSocket (STOMP) and Redis pub/sub |
| **3** | RabbitMQ and a notification worker (email becomes asynchronous) |
| **4** | Observability: Prometheus, Grafana, structured logging; optionally Kubernetes |
| Later | Shared expenses (deferred, not cancelled) |

### Phase 1: vertical slices

Each slice is a thin, complete path from the database to the browser, built test-first.

| # | Slice | State |
|---|---|---|
| 1 | Skeleton: Spring Boot + Postgres via Compose, Vite app, CI that builds | ✅ done |
| 2 | Health check: `GET /api/health-check`, frontend shows "API: connected" | ✅ done |
| 3 | Auth: email OTP login, session cookie, `GET /api/me` | 🚧 in progress |
| 4 | Create a trip (plus the frontend test harness) | planned |
| 5 | Invite link and membership | planned |
| 6 | Proposals and voting with a deadline | planned |
| 7 | Itinerary CRUD with `version` and `position` built in from the start | planned |

Phase 1 is done when all seven slices are merged, each with unit, integration and end-to-end coverage; CI is green on `main` and on every PR; and `docker compose up` gives a working app from a clean clone.

---

## Design decisions

### Authentication: email one-time code + server-side session

You sign in by typing a 6-digit code sent to your email. After that you're logged in through a session cookie; there are no JWTs.

- **Why OTP instead of Google OAuth.** There's no client secret in a public repo and no third party in the login path. The end-to-end test reads the real code from a development mailbox (Mailpit), so the app needs no test-only login backdoor.
- **Why sessions instead of JWTs.** A session can be revoked on the server. The cookie also travels with the phase-2 WebSocket handshake automatically.
- **A 6-digit code is only about 20 bits,** so the controls around it are what make it secure:
  - a small attempt limit, after which the code is invalidated
  - the code only works in the browser session that requested it
  - short expiry and single use, enforced by a conditional update
  - the same response whether or not the email is registered
  - rate limiting per email address and per IP
- **Sign-in steps.** The session is set up by hand after a code is verified: the session ID is rotated to prevent session fixation, and the security context is saved explicitly.

### Live itinerary: field-level optimistic concurrency + fractional indexing

The one genuinely hard feature is two people editing the same day at the same time. Three options were considered:

- **Whole-record last-write-wins:** rejected. Concurrent edits to different fields would silently overwrite each other.
- **A CRDT library (Yjs / Automerge):** rejected for now. It's the right tool for collaborative text, but it doesn't map cleanly onto a relational itinerary.
- **Chosen:**
  - **Versioned field edits.** Clients send operations (`set item 4 time = 14:00`), not whole documents. The server applies an operation if the `version` matches, then broadcasts it. Edits to different fields both succeed. For the same field, the last write to reach the server wins, and the other client is corrected by the broadcast.
  - **Fractional position keys** for ordering (`"a0"`, `"a05"`, `"a1"`) instead of integer indices. Two people inserting near the same spot never touch each other's rows.

This works over plain REST in phase 1 and becomes live in phase 2 without changing the model.

### Persistence: `JdbcClient` over JPA

The `WHERE version = ?` check is the core of the concurrency model, so it's written explicitly in SQL rather than handled out of sight by Hibernate's `@Version`.

### Errors as values

Expected failures in the domain are returned as a `Result` rather than thrown. Verifying a code, for example, can fail with *wrong code*, *expired*, *too many attempts* or *wrong session*, and the caller has to handle each one. Exceptions are kept for bugs and broken invariants.

---

## Testing strategy

- **Unit tests** (`src/test`). Pure rules with no I/O, such as code generation, expiry and attempt limits, using fakes and a controllable clock.
- **Web-slice tests.** `@WebMvcTest` runs the real security filter chain. Tests use real principals rather than `@WithMockUser`.
- **Integration tests** (`src/integrationTests`, a separate Gradle source set). Run against real Postgres via Testcontainers.
- **One growing journey test** (Playwright). A single end-to-end test that gains a step with each slice: the app loads, you log in, you create a trip, and so on. When it fails, the failure points at the slice just written.

---

## License

Not yet chosen.
