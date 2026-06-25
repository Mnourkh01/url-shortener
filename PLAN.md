# PLAN — URL Shortener

## Product
A small, production-shaped URL shortener service: create short links, redirect
visitors to the original URL on the hot path, and report click analytics. Built
to demonstrate clean REST design, caching, and data modeling.

## Domain glossary
- **Link** — a mapping from a short `code` to an `originalUrl`.
- **Code** — the short identifier in the path (`/{code}`); random base62 or a
  user-supplied custom alias.
- **Click** — one resolution of a code to its URL; stored for analytics.

## Architecture
Layered (this is CRUD with one hot read path, so layered is the right altitude —
no Clean Architecture ceremony):

`Controller → Service → (Repository | Cache | ClickCounter)`

- **Postgres** is the source of truth (links, clicks), via Spring Data JPA + Flyway.
- **Redis** serves the hot path: cache-aside for `code → originalUrl` and an
  atomic `INCR` live click counter. Both sit behind interfaces with in-memory
  implementations so tests and CI need no Redis.

## Non-functional requirements
- Redirect is the hot path: served from Redis cache, not a DB round-trip per hit.
- Input validated at the boundary (URL format, alias charset/length, uniqueness).
- Link creation is rate-limited per client IP (Bucket4j) to deter abuse.
- Consistent JSON error envelope for every failure.
- Health/readiness via Actuator; API documented via OpenAPI/Swagger UI.

## Quality
- Unit tests for code generation, alias validation, and service logic.
- Integration tests over the real HTTP stack on H2 + in-memory cache.
- CI (GitHub Actions) runs the full build + tests on every push.

## Deploy
- `docker-compose.yml` brings up Postgres + Redis for local/dev.
- Container-friendly (12-factor config via env). Deploy target: Render / Fly / Railway.

## Phased delivery

### v1 (this milestone) — EXIT CRITERIA
- [ ] `POST /api/v1/links` creates a link (random code or custom alias, validated).
- [ ] `GET /{code}` redirects (302), cache-aside via Redis, records a click.
- [ ] `GET /api/v1/links/{code}` returns metadata + total + live counter + last-7-days.
- [ ] `DELETE /api/v1/links/{code}` removes a link.
- [ ] Flyway migrations; rate limit on create; error envelope; Swagger; health.
- [ ] docker-compose (pg+redis); CI green; tests green with no external services.

### v2 (backlog)
- API keys / per-user links and ownership.
- Asynchronous click ingestion (queue) instead of synchronous insert.
- Expiring links + QR code generation.
- Distributed rate limiting (Redis-backed Bucket4j).
