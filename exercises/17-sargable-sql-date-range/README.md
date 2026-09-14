# Query Orders Through a Sargable Date Range

**Category:** Relational data access and performance  
**Difficulty:** Advanced  
**Estimated time:** 100 minutes

## Scenario, symptoms, and business impact

The finance team asks for every order created on a customer's local calendar
day. The current query returns the right rows, but wraps `created_at` in a
time-zone and date expression. PostgreSQL must evaluate that expression for
each candidate row instead of seeking through the `created_at` index. As the
order table grows, routine exports compete with write traffic for CPU and I/O.

## Learning objectives and prerequisites

Model a local date as a half-open instant range, keep an indexed timestamp
column bare in a predicate, and distinguish a query-shape proof from a
wall-clock claim. Complete exercises 11 and 16 first.

## Underlying cause without revealing the solution

A predicate can be logically correct yet non-sargable when it applies a
function or cast to the indexed column. The database cannot generally turn
that transformed value back into a simple B-tree range scan.

## System invariants

- A requested local day includes its first instant and excludes the next day's
  first instant.
- The America/New_York 2024-03-10 spring-forward day is exactly 23 hours.
- The lookup preserves `id`, `created_at`, and `total_cents` values.
- The lookup issues one database statement for one requested range.

## Delivery and consistency guarantees

The lookup reads committed PostgreSQL rows visible to its statement. It does
not create a cross-request snapshot, an event-delivery guarantee, or a cache
coherency guarantee.

## Process-local versus distributed guarantees

The Java range calculation is deterministic in one process. The SQL shape and
plan proof apply to the local schema and fixture only; they do not establish an
RDS latency, multi-region consistency, or production throughput guarantee.

## Functional requirements

Implement `JdbcOrderLookup.findCreatedOn(OrderDateRange)` so it returns rows
whose `created_at` belongs to the supplied half-open instant range, ordered by
`created_at, id`. Keep the existing public types and the local-date factory.

## Non-functional requirements and resource limits

Use one SQL statement and a parameterized predicate that can use the supplied
`(created_at, id)` B-tree index. Do not add a server, ORM, cache, or scheduled
job: a `JdbcTemplate` application class is sufficient for this bounded read.

## Constraints and forbidden shortcuts

- Do not compare `created_at` after casting it to `date`.
- Do not apply `AT TIME ZONE`, `date(...)`, or another function to
  `created_at` in the filter.
- Do not rely on the JVM default time zone.
- Do not use elapsed-time assertions; plans and statement counts are the
  evidence here.

## Architecture or sequence diagram

```text
local date + IANA zone
          |
          v
 [startInclusive, endExclusive) instants
          |
          v
 JDBC prepared statement: created_at >= ? AND created_at < ?
          |
          v
 PostgreSQL B-tree range scan -> ordered orders
```

## Deterministic failure reproduction

Run the explicit starter acceptance task. It captures the SQL sent to JDBC and
fails because the starter transforms `created_at` into a local date. The DST
range contract itself remains green, isolating the defect to query shape.

## Task and automated acceptance criteria

Make the starter acceptance task green without changing public APIs. The final
query must bind the two precomputed instant boundaries, leave `created_at`
untransformed in the filter, return the delegate's rows, and pass the tagged
PostgreSQL statement-count and JSON-plan integration test.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:17-sargable-sql-date-range:starter:starterAcceptanceTest
./gradlew verifyExercise17
./gradlew :exercises:17-sargable-sql-date-range:solution:integrationTest
```

Docker is required only for the last command.

## Progressive hints

1. Print the two instants made for 2024-03-10 in America/New_York.
2. Ask whether the index key appears bare on both sides of the predicate.
3. Bind the precomputed boundaries and use a half-open comparison.

## Common wrong solutions and why they fail

- `created_at::date = ?` has the same column-transformation problem and uses
  the database session time zone.
- A fixed `plusHours(24)` boundary loses or gains rows on DST transitions.
- A functional index can be a valid deliberate design, but hides the general
  range-predicate lesson and adds another index to maintain.
- A timing threshold is noisy and cannot prove that the intended index is
  eligible.

## Production and emulator boundaries

Testcontainers verifies PostgreSQL SQL wiring and one JSON plan on local,
synthetic statistics. It cannot prove managed-database failover, production
cardinality estimates, storage cache behavior, network latency, or an SLO.
Monitor slow query fingerprints, index scans, buffer reads, and pool wait time
with bounded labels in a real deployment.

## Specialist extension

Compare this range predicate with a deliberately maintained functional index
and with a stored generated local-date column. Explain write amplification,
time-zone policy, schema ownership, and how each option behaves when the
business changes its reporting zone.

## Interview follow-up questions

- What does “sargable” mean, and when may a planner still choose a sequential
  scan?
- Why is an exclusive upper boundary safer than `23:59:59.999`?
- How would tenant filtering change the composite index and the query?

## Reflection and future-post evidence

Share the before/after predicate, the New York DST boundary, and the one-query
counter. State that the JSON plan proves local index eligibility, not a
production latency improvement.

## Primary references

- [PostgreSQL: indexes and `ORDER BY`](https://www.postgresql.org/docs/current/indexes-ordering.html)
- [PostgreSQL: `EXPLAIN`](https://www.postgresql.org/docs/current/using-explain.html)
- [Java `LocalDate.atStartOfDay`](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/LocalDate.html#atStartOfDay(java.time.ZoneId))
- [Spring Framework JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
