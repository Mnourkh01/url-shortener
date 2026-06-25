# CLAUDE.md — url-shortener

## What this is
A Spring Boot + Kotlin URL shortener service. Create short links, redirect on the
hot path (Redis cache-aside), and report click analytics. See `PLAN.md` for scope
and `docs/system.md` for the architecture diagram.

## Stack
- Kotlin 2.0.21 on JDK 21, Spring Boot 3.4.1
- PostgreSQL + Spring Data JPA, Flyway migrations
- Redis (cache + atomic click counter) via spring-data-redis (Lettuce)
- Bucket4j rate limiting, springdoc OpenAPI, Actuator
- Gradle (Kotlin DSL)

## Commands
- Build + test: `./gradlew build`
- Test only: `./gradlew test`
- Run: `./gradlew bootRun --args='--spring.profiles.active=local'`
- Local infra: `docker compose up -d` (Postgres + Redis)
- JDK 21 required. On this machine: `JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot`.

## Conventions
- Layered: `controller → service → repository`. Redis and click counting sit
  behind `LinkCache` / `ClickCounter` interfaces with in-memory impls under the
  `test` profile, so tests and CI need no Redis.
- Validate at the boundary with `@Valid` DTOs. Domain errors are thrown as typed
  exceptions and rendered by `GlobalExceptionHandler` into one JSON envelope.
- Migrations are immutable once shipped; add a new `V{n}__*.sql` instead of editing.

## Notes / gotchas
- A native PostgreSQL may occupy host port 5432 on this machine. If a local run
  can't authenticate, point at a free port (e.g. `DB_PORT=5544`,
  `DB_URL=jdbc:postgresql://localhost:5544/shortener_db`).
