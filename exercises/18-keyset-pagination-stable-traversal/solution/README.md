# Reference Solution: Watermarked Composite Keyset

The solution captures the tenant's maximum ingestion sequence only when the
cursor is absent. It then reads by `created_at DESC, id DESC`, carrying the
watermark and final event position in a URL-safe Base64 cursor. Later pages
apply both `ingest_sequence <= watermark` and the strict composite keyset
predicate, so an event ingested after page one cannot shift or enter this
traversal. Requesting `pageSize + 1` rows makes cursor emission exact without a
separate existence query.

`id` is the tie-breaker because timestamps are not unique. The ingestion
sequence is intentionally distinct from display order: a valid late/backfilled
event can have an older timestamp yet a newer ingestion sequence, and therefore
belongs to a later traversal rather than the one already in progress.

The PostgreSQL integration test asserts index compatibility rather than elapsed
time. It temporarily disables sequential scans and checks the named composite
index in `EXPLAIN (FORMAT JSON)`. That proves this query shape can use the
index in the fixture; actual planner decisions still depend on table size,
statistics, parameter distributions, and database configuration.

The cursor validates its version and tenant but is not signed. Base64 does not
authorize a read or conceal data. A production endpoint needs tenant
authorization before access, a suitable composite index, cursor-size/version
limits, a retention/export policy, and monitoring for query/result sizes. For a
long-lived immutable export, a durable server-side job is often a better
contract than an API cursor.

- [PostgreSQL query planning](https://www.postgresql.org/docs/current/using-explain.html)
- [Spring JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
