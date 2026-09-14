# Stable Audit-Feed Pagination

**Category:** Relational data access and consistency  
**Difficulty:** Advanced  
**Estimated time:** 120 minutes

## Scenario, symptoms, and business impact

An audit-feed API uses `LIMIT ... OFFSET`. When a newer event arrives between
page requests, entries shift: a client can receive a duplicate and miss an
older event. Long exports also make the database discard progressively more
rows for every later offset.

## Learning objectives and prerequisites

Replace offset traversal with a composite keyset and state the traversal
semantics precisely. Complete exercises 16 and 17 first.

## Underlying cause without revealing the solution

An offset is a position in a changing ordered result. It is not the identity of
the last event a client saw, nor does it establish which events belong to an
in-progress traversal.

## System invariants

- Events are ordered by `created_at DESC, id DESC` inside one tenant.
- A traversal returns every event visible at its first request at most once.
- Events ingested after that first request are excluded from that traversal.
- A cursor cannot be replayed for a different tenant.

## Delivery and consistency guarantees

The high watermark is a per-tenant ingestion-sequence boundary, not a
cross-tenant snapshot or a durable export job. It assumes new ingestion assigns
a strictly increasing sequence. It does not provide exactly-once delivery to a
client that loses its cursor.

## Process-local versus distributed guarantees

The cursor is stateless application data, so multiple application instances can
continue a traversal. It is opaque, not signed: it is not an authorization
token. Production systems must authorize the tenant separately and protect a
cursor if its contents or tamper resistance matter.

## Functional requirements

Keep `EventFeedService.page(UUID, int, Optional<String>)`. On the first page,
capture a high watermark. Later pages must use the watermark plus the final
`(created_at, id)` position, must fetch one extra row to decide whether a next
cursor exists, and must reject malformed or cross-tenant cursors.

## Non-functional requirements and resource limits

Allow page sizes from 1 through 100. The deterministic acceptance test requires
one watermark read, no offset read, and exact traversal after an insertion. The
PostgreSQL test inspects an index-compatible plan; it makes no latency claim.

## Constraints and forbidden shortcuts

Do not keep an offset in an encoded cursor, read all rows into memory, rely on
timestamp uniqueness, or claim a database transaction remains open between
HTTP requests. Do not treat Base64 as encryption or authorization.

## Architecture or sequence diagram

```text
first page -> tenant high watermark + keyset query -> opaque cursor
next page  -> decode tenant/watermark/last key -> bounded keyset query
```

## Deterministic failure reproduction

The starter reads two entries with offset zero. A newer entry is inserted; its
second offset page returns the previous page's final event again. The same test
requires the original five IDs exactly once.

## Task and automated acceptance criteria

Make `starterAcceptanceTest` green while retaining the service API. The
solution must use the high-watermark/keyset store methods, emit no offset calls,
and pass malformed-cursor, tenant-isolation, and mutation-traversal checks.

## Exact build, test, and integration commands

```shell
./gradlew :exercises:18-keyset-pagination-stable-traversal:starter:starterAcceptanceTest
./gradlew verifyExercise18
./gradlew verifyIntegrationExercise18
```

Docker is required only by the final command.

## Progressive hints

1. Which two values form a stable strict order when timestamps tie?
2. What monotonic value separates events already in the feed from later ingestion?
3. A next cursor needs both that boundary and the final returned key.

## Common wrong solutions and why they fail

- Offset in Base64 is still offset and shifts under inserts.
- A timestamp-only cursor skips or duplicates tied timestamps.
- A keyset without an ingestion boundary admits backdated new events.
- Holding a repeatable-read transaction across requests consumes scarce database
  resources and is not a general API design.

## Production and emulator boundaries

The PostgreSQL container validates SQL syntax, mapping, and an index-compatible
plan with sequence scans disabled. It cannot prove production planner choices,
table statistics, replica lag, cursor retention policy, authorization, or a
multi-region snapshot.

## Specialist extension

Compare this watermarked traversal with a server-side export job, PostgreSQL
snapshot export, and a time-bounded feed. Explain deletion semantics, tenant
partitioning, cursor signing/key rotation, and how a late backfill gets a new
ingestion sequence.

## Interview follow-up questions

- Why does keyset pagination need a deterministic tie-breaker?
- When does a high-watermark sequence differ from the display timestamp?
- Which index matches this filter and ordering, and what can an `EXPLAIN` test not prove?

## Reflection and future-post evidence

Show the duplicate produced by offset pagination, then the IDs from the
watermarked traversal. State the mutation model and avoid claiming universal
snapshot consistency or a performance number from the local plan.

## Primary references

- [PostgreSQL `SELECT`](https://www.postgresql.org/docs/current/sql-select.html)
- [PostgreSQL indexes](https://www.postgresql.org/docs/current/indexes.html)
- [Spring JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
