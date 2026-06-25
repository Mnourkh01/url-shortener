# System

```mermaid
flowchart LR
    Client[Client / Browser]

    subgraph App[URL Shortener service]
        Create[POST /api/v1/links]
        Redirect[GET /code]
        Stats[GET /api/v1/links/code]
        LinkSvc[LinkService]
        ClickSvc[ClickService]
    end

    subgraph Data
        PG[(PostgreSQL\nlinks, clicks)]
        Redis[(Redis\nurl cache + INCR counter)]
    end

    Client -->|create| Create --> LinkSvc
    Client -->|visit short link| Redirect --> LinkSvc
    Client -->|view stats| Stats --> LinkSvc

    LinkSvc -->|read-through cache| Redis
    LinkSvc --> PG
    Redirect --> ClickSvc
    ClickSvc -->|persist click| PG
    ClickSvc -->|INCR live counter| Redis
    LinkSvc -.302 redirect.-> Client
```

## Hot path: `GET /{code}`
1. `LinkService.resolve(code)` checks Redis (`url:{code}`); on miss, reads Postgres
   and populates the cache with a TTL.
2. `ClickService.record(...)` inserts a `clicks` row and `INCR`s the Redis live
   counter `clicks:{code}`.
3. Respond `302 Found` with `Location: originalUrl`.

Postgres remains the source of truth for analytics; Redis only accelerates reads.
