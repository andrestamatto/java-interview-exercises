# Bound JPA Order-Summary Queries

**Category:** Data access and ORM performance  
**Difficulty:** Advanced  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

An order-history export returns correct totals but performs one line lookup for
every returned order. A page of 200 orders becomes 201 database round trips,
exhausting pool capacity and increasing tail latency during normal traffic.

## Learning objectives and prerequisites

Identify N+1 as query-count growth, make a read use case explicit, and prove a
bounded SQL budget. Complete exercises 11 and 15 first.

## Underlying cause without revealing the solution

The starter loads headers and then asks the store for each order's lines while
building a response. Lazy associations make the equivalent JPA failure easy to
introduce; serialization and OSIV only move the hidden query elsewhere.

## System invariants

- Each returned order has the correct customer and total in cents.
- Empty line collections total zero.
- The summary path has a bounded number of select statements as order count grows.

## Delivery and consistency guarantees

This is a read of committed relational data. It provides no cross-request
snapshot, write transaction, event delivery, or cache-consistency guarantee.

## Process-local versus distributed guarantees

The query budget is evidence for one code path against one database schema. It
does not limit queries from other replicas, endpoints, interceptors, or clients.

## Functional requirements

Keep `OrderSummaryService.summaries()` and preserve totals/order. Replace the
per-order line access with one explicit summary loading operation. The JPA
adapter must fetch all required rows without a globally eager association.

## Non-functional requirements and resource limits

The deterministic acceptance test allows one summary load and no per-order line
loads. The PostgreSQL integration test allows at most one select after fixtures
are flushed and cleared. Do not use elapsed time as evidence.

## Constraints and forbidden shortcuts

Do not disable lazy loading globally, enable OSIV, remove totals, alter the
counter, or cache test data. A fetch join/projection belongs to this read use
case; it is not a universal mapping policy.

## Architecture or sequence diagram

```text
summary request -> explicit summary query -> orders + required lines -> immutable summaries
```

## Deterministic failure reproduction

The starter acceptance store records one `loadLines` call for each header and
fails the fixed summary-query budget while still proving output correctness.

## Task and automated acceptance criteria

Make starter acceptance green without changing its public API. The solution
must pass the pure contract test and the tagged PostgreSQL integration test.

## Exact build, test, and integration commands

```shell
./gradlew :exercises:16-jpa-order-summary-n-plus-one:starter:starterAcceptanceTest
./gradlew verifyExercise16
./gradlew :exercises:16-jpa-order-summary-n-plus-one:solution:integrationTest
```

Docker is required only for the final command.

## Progressive hints

1. Count calls made while mapping one page.
2. Model the summary read as a single store operation.
3. Use a JPQL fetch join or aggregate projection scoped to this response shape.

## Common wrong solutions and why they fail

- `FetchType.EAGER` changes every use of an entity and can overfetch elsewhere.
- OSIV defers the query rather than removing it.
- A cache can hide the test but does not repair a cold-path query budget.
- A wall-clock assertion confuses fixture speed with query shape.

## Production and emulator boundaries

PostgreSQL/Testcontainers proves local ORM and SQL wiring, not RDS failover,
production statistics, network latency, IAM, or a workload SLO. Monitor query
count, pool waiting, and result sizes with bounded labels.

## Specialist extension

Compare an aggregate DTO projection, fetch join, and Hibernate batch fetching.
Explain duplicate-row memory cost, pagination interaction, index needs, and why
the appropriate option depends on the response shape.

## Interview follow-up questions

- Why is OSIV not an N+1 fix?
- When is a fetch join incompatible with paging?
- Which statement budget would you expose as an operational signal?

## Reflection and future-post evidence

Capture the failing query counter, the green bounded counter, and the exact
scope of the claim. Do not turn a local select count into a latency claim.

## Primary references

- [Hibernate fetching](https://docs.hibernate.org/orm/current/introduction/html_single/Hibernate_Introduction.html#fetching)
- [Spring Data JPA reference](https://docs.spring.io/spring-data/jpa/reference/)
- [PostgreSQL documentation](https://www.postgresql.org/docs/current/)
