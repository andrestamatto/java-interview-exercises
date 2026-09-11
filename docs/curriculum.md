# Detailed Curriculum Catalog

Each entry fixes the incident, invariant, deterministic evidence, prerequisites,
and implementation boundary before code is written. `Advanced` exercises target
senior fundamentals; `Specialist` exercises add deeper runtime/distributed proof.

## 01–10 — Concurrency and Java Memory Model

| ID and slug | Level / time | Incident, invariant, and deterministic proof | Stack / prerequisites |
|---|---|---|---|
| 01 `lost-update-stock-reservation` | Advanced / 75m | Flash-sale check/decrement loses stock. Preserve `remaining = initial - successes`, never negative. A barrier forces two reads before writes; property tests compare with a sequential model. | Plain Java, JUnit, jqwik / none |
| 02 `volatile-shutdown-visibility` | Advanced / 75m | A worker may not observe shutdown. Establish a happens-before edge without claiming `volatile` makes compound state atomic. Structural checks plus jcstress show the contract honestly. | Plain Java, jcstress / 01 |
| 03 `atomic-quota-compound-operation` | Advanced / 90m | `AtomicInteger` check-then-decrement exceeds quota. A barrier exposes the last-slot race; CAS transition and state-machine properties prove bounds. | Plain Java, jqwik / 01–02 |
| 04 `immutable-snapshot-safe-publication` | Advanced / 90m | Readers observe later mutation of published discount rules. A retained snapshot is mutated after a latch; deep immutability and atomic publication preserve self-consistency. | Records, immutable collections / 02 |
| 05 `deadlock-free-account-transfer` | Advanced / 105m | Opposite transfers lock accounts in opposite order. Forced dual lock acquisition and `ThreadMXBean` expose the cycle; total order preserves balance and release-on-failure. | `ReentrantLock`, JFR optional / 01 |
| 06 `reduce-receipt-lock-contention` | Specialist / 105m | Correct receipt generation serializes signing inside a monitor. Concurrent invariant tests protect unique sequence/signature; supplied JMH demonstrates scaling as the critical section shrinks. | JMH, optional JFR / 01,05 |
| 07 `bounded-import-executor` | Advanced / 90m | An unbounded executor queue retains an import burst. Latches saturate workers and prove exact accepted/rejected counts, bounded queue, and shutdown policy. | `ThreadPoolExecutor`, Micrometer optional / 01 |
| 08 `virtual-threads-with-downstream-bulkhead` | Specialist / 120m | Virtual threads remove an accidental throttle and overwhelm 20 downstream connections. A controlled dependency proves max in-flight and permit cleanup. Java 21 captures pinning; Java 25 extension contrasts JEP 491 behavior. | Virtual threads, semaphore, JFR / 05,07 |
| 09 `completable-future-task-lifetime` | Specialist / 120m | A timed-out fan-out leaves sibling work running. Controlled futures prove deadline, cancellation, no orphan work, and root-cause preservation. Optional Java 25 Structured Concurrency remains preview-isolated. | `CompletableFuture`; Java 25 extension / 07–08 |
| 10 `async-request-context` | Advanced / 105m | Tenant/correlation context disappears or leaks on reused workers. A single-thread executor deterministically proves loss/leak and exceptional cleanup. Java 25 compares final Scoped Values within their inheritance contract. | `ThreadLocal`, OTel context / 04,09 |

Specialist depth: 05, 06, 08, 09, and 10 include diagnostic or Java-version
comparisons. JMM acceptance never depends on probabilistically observing a stale
read.

## 11–20 — Performance and data access

| ID and slug | Level / time | Incident, invariant, and deterministic proof | Stack / prerequisites |
|---|---|---|---|
| 11 `index-membership-lookups` | Advanced / 75m | Authorization reconciliation performs `N×M` comparisons. An operation counter proves complexity; differential properties preserve request order, duplicates, and results. | Collections, jqwik / 04 |
| 12 `trustworthy-jmh-benchmark` | Advanced / 105m | A hand timer favors dead-code-eliminated work. Learner fixes state scope, consumption, warmup, parameters, forks, units, and result interpretation; no test requires one implementation to win. | JMH / 11 |
| 13 `reduce-event-parser-allocation` | Specialist / 105m | Parser substrings/maps create high bytes/op and retain oversized input. Differential tests protect behavior; JMH GC profiler and retention evidence separate allocation from latency. | JMH, JFR / 12 |
| 14 `primitive-metric-window` | Advanced / 90m | Millions of boxed `Long` values inflate allocation and hurt locality. Boundary/overflow tests and JMH bytes/op compare a primitive representation without invented value-class claims. | Arrays/buffers, JMH / 12–13 |
| 15 `deadline-ordered-job-collection` | Advanced / 105m | An `ArrayList` scheduler scans and shifts for every deadline. Model-based mixed operations preserve `(deadline,id)` order and idempotent cancellation; JMH compares realistic ratios. | Priority queue + ID index, jqwik / 11–14 |
| 16 `jpa-order-summary-n-plus-one` | Advanced / 105m | Serialization executes `1+N` line queries. PostgreSQL integration asserts correct totals and a bounded statement budget without OSIV as a fix. | Spring Data JPA, PostgreSQL / 11,15 |
| 17 `sargable-order-date-query` | Specialist / 120m | A function on indexed timestamp causes a sequential scan. Time-zone boundary tests and JSON `EXPLAIN ANALYZE` prove equivalent rows and useful index access without elapsed-time gates. | Spring JDBC, PostgreSQL / 11,16 |
| 18 `keyset-order-history-pagination` | Specialist / 120m | Deep offsets slow down and concurrent inserts shift pages. Controlled insertion proves drift; stable `(created_at,id)` cursors prove bounded pages/no duplicates under stated snapshot semantics. | Spring JDBC, PostgreSQL / 17 |
| 19 `dynamodb-batch-read-round-trips` | Specialist / 120m | One `GetItem` per SKU exhausts the deadline. Interceptors count calls; scripted `UnprocessedKeys` proves bounded continuation and ≤100 keys/request. | AWS SDK v2, DynamoDB Local / 07,11 |
| 20 `database-pool-admission-control` | Specialist / 120m | Transactions hold a two-connection pool across remote work. Latches force saturation; Toxiproxy injects connection loss to prove bounded acquisition, rollback/release, recovery, and safe retry boundaries without claiming RDS failover fidelity. | Spring JDBC, Hikari, PostgreSQL, optional Toxiproxy / 07,16–17 |

JMH is required for 06 and 12–15. Exercises 17–20 use database plans,
request counts, saturation state, and load evidence rather than misleading
microbenchmarks.

## 21–30 — Resilience and resource protection

| ID and slug | Level / time | Incident, invariant, and deterministic proof | Stack / prerequisites |
|---|---|---|---|
| 21 `end-to-end-timeout-budget` | Advanced / 90m | Three individually timed calls exceed the endpoint SLO. A manual monotonic ticker proves every call/backoff receives only remaining deadline and resources release on cancellation. | Spring MVC, `RestClient`/HTTP interface, WireMock / 07,09,20 |
| 22 `safe-retries-with-jitter` | Advanced / 90m | Fixed retry waves block recovery and retry 400s. Fake sleeper/random assert exact capped exponential jitter, `Retry-After`, classification, interruption, and total-budget behavior. | Plain policy core + Spring Boot/Resilience4j adapter, WireMock / 21 |
| 23 `idempotent-http-commands` | Advanced / 120m | A committed transfer's lost response causes a second transfer. Concurrent PostgreSQL requests prove tenant-scoped key/fingerprint, atomic stored response, conflict, rollback, and restart replay. | Spring MVC/JDBC, PostgreSQL / 01,03,16,21–22 |
| 24 `circuit-breaker-recovery` | Advanced / 75m | A breaker counts business 404s and never recovers. Manual clock and latches prove failure classification, open rejection, bounded half-open probes, and transitions. | Spring Boot, Resilience4j, Actuator / 21–22 |
| 25 `bulkheads-and-failure-isolation` | Advanced / 75m | Slow recommendations consume payment capacity. Blocked recommendation calls prove dependency-specific capacity, bounded wait/rejection, and permit cleanup on every path. | Spring Boot, virtual threads, Resilience4j / 07–08,20–21 |
| 26 `rate-limiting-and-fairness` | Advanced / 90m | One tenant consumes a global burst. Manual time/property tests prove token conservation, tenant isolation, fair policy, bounded state, and correct `Retry-After`. | Plain Java + Spring filter; Redis extension / 03,07,21 |
| 27 `load-shedding-bounded-queues` | Advanced / 75m | A queue accepts work already too old to matter. Exact queue-depth/deadline tests prove bounded admission, fast observable rejection, cleanup, and readiness semantics. | Executor, Spring MVC, Micrometer / 07,20–21,25–26 |
| 28 `bounded-stale-cache-fallback` | Advanced / 90m | Fallback serves weeks-old prices and masks 403. Fake clock/failure matrix proves bounded age, eligible failures only, freshness disclosure, and concurrent refresh. | Spring Boot, Caffeine, WireMock / 21,24–25 |
| 29 `partial-failure-fanout-http-clients` | Specialist / 120m | Optional recommendation failure erases successful required branches. Controlled futures prove result policy, one deadline, cancellation, and no orphan work; client choice is justified, not universal. | Spring MVC; HTTP interfaces/OpenFeign/WebClient comparison / 08–09,21–25,28 |
| 30 `graceful-shutdown-and-draining` | Advanced / 90m | Deployment kills accepted committing requests. A commit latch proves readiness-before-drain, no new admission, bounded completion/cancellation, and dependency shutdown order. | Spring lifecycle, Actuator / 07,09,21,27 |

## 31–40 — Consistency and messaging

| ID and slug | Level / time | Incident, invariant, and deterministic proof | Stack / prerequisites |
|---|---|---|---|
| 31 `dual-write-transactional-outbox` | Advanced / 105m | Process dies after order commit but before event publish. Seam failures prove aggregate and stable outbox intent commit/rollback atomically; no exactly-once claim. | Spring JDBC, PostgreSQL / 16,20,23 |
| 32 `reliable-outbox-publication` | Specialist / 120m | Publish succeeds but acknowledgement/marking is lost. A send-then-throw port proves restart duplicates with the same event ID, retry visibility, and safe multi-relay progress. | Spring Boot/JDBC, PostgreSQL + SQS emulator, AWS SDK v2 / 22,31 |
| 33 `idempotent-consumer-inbox` | Specialist / 120m | Worker commits shipment then dies before message deletion. Duplicate/concurrent/restart tests prove inbox + business effect atomicity and delete-after-commit. | Spring Boot/JDBC, PostgreSQL + SQS emulator / 23,31–32 |
| 34 `optimistic-concurrency-conditional-writes` | Advanced / 105m | Two stale inventory updates oversell. Barrier-controlled writers prove version monotonicity, stale rejection, and business-rule re-evaluation; DynamoDB extension compares conditional writes. | Spring Boot/JDBC, PostgreSQL; DynamoDB Local extension / 01,03,16,23 |
| 35 `pessimistic-locking-and-contention` | Specialist / 120m | Reversed multi-SKU locks deadlock while payment runs inside the transaction. Real database locks prove stable ordering, bounded waits, short scope, rollback, and pool release. | Spring Boot/JDBC, PostgreSQL / 05,20,34 |
| 36 `durable-saga-orchestration` | Specialist / 120m core | In-memory orchestration forgets a reservation after crash. A durable state-transition table proves restart, idempotent compensation, late/duplicate reply handling, terminal monotonicity, and operator recovery. | Spring Boot bounded contexts, supplied outbox/inbox, PostgreSQL, queues / 30–35 |
| 37 `event-ordering-and-partition-keys` | Specialist / 120m | Aggregate events keyed by event ID land on different Kafka partitions and regress a projection. Controlled reorder plus broker integration prove aggregate keying and monotonic versions. | Spring Kafka, KRaft Testcontainer / 09,31–33 |
| 38 `sqs-visibility-poison-dlq` | Specialist / 120m | Processing exceeds visibility and poison messages consume workers. Explicit visibility expiry proves duplicates; tests cover renewal, delete-after-commit, long-poll cancellation, receive count, DLQ, batch partial success, and shutdown. | Spring Boot lifecycle, AWS SDK v2, SQS emulator / 22,30,33 |
| 39 `backward-compatible-event-contracts` | Advanced / 105m | Renamed fields break legacy billing and fan-out filters. Golden v1 consumers/routing fixtures prove additive evolution, tolerant readers, stable envelopes, upcasting, and duplicate tolerance. | Jackson/schema validation, SNS/EventBridge emulator or Kafka / 19,32–33,37 |
| 40 `s3-event-cache-invalidation` | Specialist / 120m | A paused v1 load repopulates cache after v2 invalidation. Barriers and synthetic duplicate/reordered sequencers prove monotonic cache updates, TTL reconciliation, and S3-as-source-of-truth; extension verifies multipart completion and checksum metadata. | Spring Boot, Caffeine, AWS SDK v2, S3/SQS emulator / 28,33,38–39 |

Messaging contracts use immutable past-tense facts with event ID, aggregate ID,
aggregate version, occurrence time, and schema version. Outbox is atomic intent,
SQS Standard is at-least-once/best-effort order, Kafka order is per partition,
and consumers remain duplicate-tolerant.

## 41–50 — Scale, observability, security, and capstone

| ID and slug | Level / time | Incident, invariant, and deterministic proof | Stack / prerequisites |
|---|---|---|---|
| 41 `stateless-horizontal-scaling` | Advanced / 90m | Replica switching loses local idempotency/session/rate state. Two deterministic app instances prove any replica can serve using external durable/distributed state. | Spring MVC, PostgreSQL/Redis / 07,20,23,30 |
| 42 `partition-skew-hot-keys` | Specialist / 120m | Tenant/date key throttles a whale tenant. Distribution properties and DynamoDB conditional conflicts prove deterministic routing, bounded fan-out, and no silent overwrite. | AWS SDK v2, DynamoDB Local, jqwik / 34,37,41 |
| 43 `cache-stampede-request-coalescing` | Advanced / 90m | Simultaneous expiry floods origin. Barriers prove one process-local in-flight load/key, bounded caller wait/fallback, retry after failure, and no cross-instance overclaim. | Caffeine; Redis extension / 06,27–28,40–41 |
| 44 `cqrs-eventual-projection` | Specialist / 120m | Projection lags/retries/duplicates after a committed order. Replay/restart prove authoritative write transaction, idempotent monotonic projection, and explicit pending/stale API state. | Spring Boot, PostgreSQL, broker / 31–34,39 |
| 45 `distributed-leases-fencing-tokens` | Specialist / 120m | Paused worker writes after another acquires an expired lease. Controlled clock proves monotonic fencing and stale-write rejection; expiry alone is never ownership proof. | PostgreSQL or Redis adapter / 05,34,41 |
| 46 `structured-logging-correlation` | Advanced / 75m | HTTP/async/message logs cannot be joined and leak MDC. Capturing sinks prove validated IDs, propagation, cleanup, redaction, and tenant-safe fields. | Spring Boot, Logback, Micrometer context / 10,21,29 |
| 47 `metrics-slis-slos-alerts` | Advanced / 105m | Average latency hides errors and saturation. Meter fixtures prove low-cardinality labels, correct ratios/histograms, queue signals, burn-rate rules, and actionable alerts. | Micrometer, Prometheus format / 20–29,46 |
| 48 `distributed-tracing-async` | Specialist / 120m | HTTP-to-message trace parentage breaks or leaks. In-memory exporter proves W3C context, correct span relationships, error/cancellation status, cleanup, and redaction. | OpenTelemetry, Spring instrumentation / 10,32,37,46 |
| 49 `multi-tenant-authorization-isolation` | Specialist / 120m | Role checks still leak tenant rows, object keys, cursors, and async context. Adversarial tokens/IDs/events prove authenticated tenant scope, deny-by-default permissions, safe errors/logs, and query/object isolation. | Spring Security, PostgreSQL, S3 adapter / 23,41,46,48 |
| 50 `compatible-cloud-document-capstone` | Specialist / 120m core + 120m extension | Two bounded contexts persist upload metadata/outbox, store/reconcile objects, deliver duplicate messages, and support v1/v2. Failure matrix proves atomic intent, idempotency, tenancy, observability, compatibility, and convergence—never distributed ACID. | Two Spring services, PostgreSQL, SQS/S3 emulators, OTel / 16,20–23,30–40,41,46–49 |

Exercise 49 must test missing/expired/forged tokens, insufficient permissions,
tenant-ID conflicts, guessed UUID/cursor/object access, query predicate omission,
async context leakage, malicious/replayed events, object-key prefix confusion,
expired presigned URLs, and sensitive error/log/trace output.

The capstone excludes UI, real-account deployment, cross-region replication,
managed RDS failover, global quotas, virus scanning, and exactly-once claims.
Earlier exercise scaffolding is supplied so its core remains bounded.

## Concrete specialist extensions

At least these extensions are part of the design contract:

1. 02: jcstress and VarHandle access-mode comparison.
2. 05: JFR/thread-dump diagnosis and multi-account lock ordering.
3. 06: lock striping, fairness, false sharing, and JMH profiling.
4. 08: Java 21 pinning evidence versus Java 25/JEP 491 behavior.
5. 09: Java 25 Structured Concurrency cancellation/lifetime comparison.
6. 10: final Scoped Values with structured lexical inheritance.
7. 13: allocation flame graph, escape analysis, and compact headers experiment.
8. 17: covering/partial indexes, statistics skew, and write amplification.
9. 20: ambiguous connection loss, replica budgets, and RDS Proxy trade-offs.
10. 23: remote payment ambiguity resolved only by provider idempotency.
11. 32: relay claim leases/fencing and database-to-broker atomicity limits.
12. 36: timeout-versus-late-success reconciliation and operator recovery.
13. 40: multipart completion, checksum mismatch, lost events, and convergence.
14. 45: stale lease-holder writes rejected by resource fencing.
15. 49: adversarial tenant/token/object/event isolation matrix.
