# Admit Capture Work Before Borrowing a Database Connection

**Category:** resilience and relational resource management  
**Difficulty:** Specialist  
**Estimated time:** 120 minutes

## Scenario, symptoms, and business impact

A payment capture opens a transaction, waits for a remote risk decision, then
writes the capture. Two slow risk calls occupy a two-connection pool. Every
new request waits behind the pool, holds application resources, and turns a
remote incident into database-pool exhaustion.

## Learning objectives and prerequisites

Separate local admission from connection acquisition, keep remote work outside
the transaction, release/roll back on pre-commit failure, and distinguish a
safe retry from an ambiguous commit. Complete exercises 07, 08, 16, and 17.

## Underlying cause without revealing the solution

A JDBC connection is a scarce, stateful resource. Holding it across remote
latency extends its lifetime from database work to end-to-end request time. A
pool timeout only limits one wait; it does not define a useful overload result
or protect work already waiting in the application.

## System invariants

- At most `maximumInFlight` capture attempts are admitted by one process.
- A remote risk check holds no database transaction or connection.
- A pre-commit failure rolls back and closes its transaction.
- An unknown commit outcome is not retried automatically.
- A rejected admission performs no database acquisition.

## Delivery and consistency guarantees

`COMMITTED` means the local JDBC `commit` returned normally. A
`COMMIT_OUTCOME_UNKNOWN` result deliberately says neither committed nor rolled
back: reconcile it through a durable idempotency key or query before another
attempt. This exercise does not send a payment to an external processor.

## Process-local versus distributed guarantees

The semaphore is a per-JVM bulkhead. It neither coordinates replicas nor
enforces a tenant-wide limit. Hikari manages local connections; PostgreSQL
transaction semantics do not make a remote risk call atomic.

## Functional requirements

Keep `CaptureService.capture(UUID)` and all public result values. Make the
acceptance tests pass: two blocked risk checks must consume zero connections,
the third request must receive `CAPACITY_REJECTED`, pre-commit failure must
roll back and release, and an ambiguous commit must not be retried.

## Non-functional requirements and resource limits

Use immediate, bounded admission. Do not make callers wait indefinitely for a
pool connection. The exercise uses a two-connection controlled pool and a
two-permit service only to make the failure reproducible; production sizing
needs workload, database, and downstream evidence.

## Constraints and forbidden shortcuts

- Do not increase the connection pool to hide remote latency.
- Do not hold a transaction through `RiskGateway.approve`.
- Do not return success after a commit connection loss.
- Do not automatically retry an ambiguous commit.
- Do not introduce an HTTP server, ORM, queue, or distributed lock.

## Architecture

```text
request -> local admission -> remote risk check -> borrow Hikari connection
        -> short JDBC transaction -> commit or rollback -> close connection
```

## Deterministic failure reproduction

The starter obtains both controlled connections before its two risk calls wait
on a latch. The third request then blocks trying to borrow a connection, and
the explicit acceptance test fails. The reference solution reaches the same
risk latch with zero borrowed connections and rejects the third request.

## Task and automated acceptance criteria

Run the explicit starter task while learning. The solution must pass the shared
controlled acceptance tests and the tagged PostgreSQL/Hikari integration test
when Docker is available.

## Exact build, test, and run commands

```shell
./gradlew :exercises:20-database-pool-admission-control:starter:starterAcceptanceTest
./gradlew verifyExercise20
./gradlew verifyIntegrationExercise20
```

Docker is required only for the last command.

## Progressive hints

1. Inspect when the starter calls `TransactionPool.acquire()`.
2. Count open connections while two risk calls wait on their latch.
3. Put a bounded local decision before the risk and JDBC paths.
4. Treat the point after `commit()` starts as an uncertain recovery boundary.

## Common wrong solutions and why they fail

- A larger pool postpones, but does not remove, the saturation mode.
- A pool timeout still creates waiting work and gives no explicit local
  admission result.
- Retrying every `SQLException` can duplicate a capture after the server
  committed but its acknowledgement was lost.
- Retrying remote work inside a JDBC transaction retains the scarce connection.
- Calling rollback after a connection-loss commit outcome cannot prove that the
  server did not commit.

## Production and emulator boundaries

The controlled transaction fake models connection loss at explicit boundaries.
The Testcontainers integration proves local PostgreSQL/Hikari wiring and one
rollback/reuse path. It does not emulate RDS failover, TLS, network partitions,
IAM authentication, production pool contention, or payment-processor effects.
Toxiproxy can be added as a specialist extension, but its local proxy behavior
is not an RDS-failover proof.

## Specialist extension

Add an idempotency table keyed by a client capture key. Define reconciliation
for `COMMIT_OUTCOME_UNKNOWN`, then use Toxiproxy to cut a PostgreSQL connection
during commit. Explain what evidence distinguishes a client-side unknown from a
database outcome and how the policy works across replicas.

## Interview follow-up questions

- Why is a pool timeout not sufficient admission control?
- Why must remote work be outside a transaction even with a large pool?
- Which outcomes can be retried safely, and how would idempotency change that?
- How would you measure pool wait, admission rejection, rollback, and unknown
  commit outcomes without high-cardinality labels?

## Reflection and future-post evidence

Share the latch timeline, the zero-connection assertion while risk is blocked,
and the explicit unknown-commit result. State that this is a process-local
pattern and not a claim of RDS failover simulation.

## Primary references

- [HikariCP configuration](https://github.com/brettwooldridge/HikariCP#gear-configuration-knobs-baby)
- [PostgreSQL transaction isolation](https://www.postgresql.org/docs/current/transaction-iso.html)
- [JDBC `Connection.commit`](https://docs.oracle.com/en/java/javase/21/docs/api/java.sql/java/sql/Connection.html#commit())
- [Spring Framework JDBC](https://docs.spring.io/spring-framework/reference/data-access/jdbc.html)
