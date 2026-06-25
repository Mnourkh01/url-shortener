# URL Shortener

A small, production-shaped URL shortener built with Spring Boot and Kotlin. Create
short links, redirect visitors on a Redis-cached hot path, and view click analytics.

Built to show clean REST design, cache-aside with Redis, sensible data modeling,
rate limiting, and a tested, CI-backed codebase, not just a toy.

## Features

- **Create links** with a generated base62 code or a custom alias (validated)
- **Fast redirects** (`GET /{code}` → 302) served cache-aside from Redis
- **Click analytics** — authoritative totals in Postgres + a live Redis counter + a 7-day breakdown
- **Rate limiting** on link creation (Bucket4j token bucket, per client IP)
- **OpenAPI / Swagger UI**, Actuator health, and a consistent JSON error envelope
- **Tested**: unit + full HTTP integration tests that run on H2 with no external services

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.0.21 |
| Framework | Spring Boot 3.4.1 |
| Java | 21 (toolchain) |
| Database | PostgreSQL + Spring Data JPA, Flyway |
| Cache | Redis (Lettuce) |
| Build | Gradle (Kotlin DSL) |
| Docs | springdoc-openapi |

## Architecture

```
Controller → Service → (Repository | LinkCache | ClickCounter)
```

Postgres is the source of truth. Redis serves the hot path: cache-aside for
`code → originalUrl` and an atomic `INCR` live counter. Both sit behind interfaces
with in-memory implementations, so tests and CI need no Redis. See
[`docs/system.md`](docs/system.md) for the diagram and [`PLAN.md`](PLAN.md) for scope.

## Quick start

### 1) Start Postgres + Redis

```bash
docker compose up -d
```

> If a local PostgreSQL already owns port `5432`, run on a free port: set
> `DB_PORT=5544` and `DB_URL=jdbc:postgresql://localhost:5544/shortener_db`.

### 2) Configure and run

```bash
cp .env.example .env        # defaults already match docker-compose
./gradlew bootRun --args='--spring.profiles.active=local'
```

- Swagger UI: `http://localhost:8080/swagger-ui.html`
- Health: `http://localhost:8080/actuator/health`

### 3) Try it

```bash
# Create a short link
curl -X POST http://localhost:8080/api/v1/links \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com/some/long/path","alias":"demo"}'
# => { "code": "demo", "shortUrl": "http://localhost:8080/demo", ... }

# Follow it (302 -> original URL)
curl -i http://localhost:8080/demo

# View analytics
curl http://localhost:8080/api/v1/links/demo
```

## API

| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/links` | Create a link. Body: `{ "url": "...", "alias?": "..." }` |
| GET | `/{code}` | Redirect (302) to the original URL; records a click |
| GET | `/api/v1/links/{code}` | Analytics: total + live counter + last 7 days |
| DELETE | `/api/v1/links/{code}` | Delete a link (cascades its clicks) |

Aliases: 3–32 chars of `[A-Za-z0-9_-]`. URLs must start with `http://` or `https://`.

## Build & test

```bash
./gradlew build      # compile + run all tests
./gradlew test       # tests only
```

Tests run entirely on in-memory H2 with in-memory cache/counter implementations
(`test` profile, Flyway off), so `./gradlew test` is green without Postgres, Redis,
or Docker. CI (GitHub Actions) runs the same on every push.

## Roadmap

- API keys / per-user links and ownership
- Asynchronous click ingestion (queue) instead of a synchronous insert
- Expiring links + QR code generation
- Distributed (Redis-backed) rate limiting for multi-instance deploys

## License

Add a license that matches your intended usage (e.g. MIT, Apache-2.0).
