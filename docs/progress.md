# Curriculum Progress

Phase 2 implements the build foundation and representative exercise 01. Every
command/date cell records reproducible evidence; intentional starter failures
remain explicit rather than being hidden from the normal build.

| ID | Concept / status | Java | Frameworks | Infrastructure | Planned test evidence | Verification command / date | Limits / emulator boundary | Senior review / delegation |
|---:|---|---|---|---|---|---|---|---|
| 01 | Lost updates / verified, review approved | 21 (Temurin 21.0.12.1+1) | Plain Java, JUnit, jqwik | none | coordinated + property | `gradlew.bat --no-daemon spotlessApply clean formatCheck compileStarters verifySolutions verifyStarterIsolation generateLicenseReport` / 2026-09-11; `starterAcceptanceTest --rerun-tasks` failed as designed | process-local; no throughput claim | Owner checkpoint approved / Sol design review |
| 02 | Volatile shutdown visibility / verified | 21 (Temurin 21.0.12.1+1) | Plain Java, jcstress | none | structural + jcstress | `gradlew.bat --no-daemon :exercises:02-volatile-shutdown-visibility:solution:jcstress` / 2026-09-11; starter acceptance fails as designed | process-local; jcstress race outcomes are not stale-read proof | Pending batch review / Sol design |
| 03 | Atomic quota compound operation / verified | 21 (Temurin 21.0.12.1+1) | Plain Java, jqwik | none | coordinated + model | `gradlew.bat --no-daemon verifyExercise03` / 2026-09-11; starter acceptance fails as designed | process-local; no distributed rate-limit claim | Pending batch review / Sol design |
| 04 | Immutable snapshot safe publication / verified | 21 (Temurin 21.0.12.1+1) | Plain Java | none | retained-mutation + validation | `gradlew.bat --no-daemon verifyExercise04` / 2026-09-11; starter acceptance fails as designed | process-local; no cross-node cache claim | Pending batch review / Sol design |
| 05 | Deadlock-free account transfer / verified | 21 (Temurin 21.0.12.1+1) | Plain Java, JFR optional | none | forced cycle + `ThreadMXBean` | `gradlew.bat --no-daemon verifyExercise05` / 2026-09-11; starter acceptance fails as designed | JVM locks only; not database locking | Pending batch review / Sol design |
| 06 | Reduce receipt lock contention / verified | 21 (Temurin 21.0.12.1+1) | Plain Java, JMH | none | coordinated concurrency + JMH | `gradlew.bat --no-daemon verifyExercise06` / 2026-09-11; starter acceptance fails as designed | machine-dependent measurement; signer policy remains external | Pending batch review / Sol design |
| 07 | Bounded import executor / verified | 21 (Temurin 21.0.12.1+1) | Plain Java | none | exact saturation | `gradlew.bat --no-daemon verifyExercise07` / 2026-09-11; starter acceptance fails as designed | process-local; no durable queue claim | Pending batch review / Sol design |
| 08 | Virtual threads with downstream bulkhead / verified | 21 (Temurin 21.0.12.1+1) | Plain Java, JFR optional | none | controlled max in-flight | `gradlew.bat --no-daemon verifyExercise08` / 2026-09-11; starter acceptance fails as designed | local bulkhead; downstream capacity remains finite | Pending batch review / Sol design |
| 09 | CompletableFuture task lifetime / verified | 21 (Temurin 21.0.12.1+1) | Plain Java | none | controlled child cancellation | `gradlew.bat --no-daemon verifyExercise09` / 2026-09-11; starter acceptance fails as designed | cancellation is cooperative; Java 25 extension remains isolated | Pending batch review / Sol design |
| 10 | Async request context / verified | 21 (Temurin 21.0.12.1+1) | Plain Java | none | reused-worker propagation + cleanup | `gradlew.bat --no-daemon verifyExercise10` / 2026-09-11; starter acceptance fails as designed | `ThreadLocal` is local; Java 25 Scoped Values are an extension | Pending batch review / Sol design |
| 11 | Lookup complexity / planned | 21 | Plain Java, jqwik | none | operation count + differential | Not run | average hash behavior | Pending / Sol design |
| 12 | JMH correctness / planned | 21 | JMH | none | smoke + JSON + semantics | Not run | no winner gate | Pending / Sol design |
| 13 | Allocation / planned | 21; 25 ext | JMH, JFR | none | GC profiler + retention | Not run | allocation ≠ latency | Pending / Sol design |
| 14 | Boxing/layout / planned | 21 | JMH | none | boundaries + bytes/op | Not run | workload-specific | Pending / Sol design |
| 15 | Collection choice / planned | 21 | jqwik, JMH | none | state model + workload | Not run | workload-specific | Pending / Sol design |
| 16 | JPA N+1 / planned | 21 | Spring Data JPA | PostgreSQL | result + query budget | Not run | local DB ≠ RDS | Pending / Sol design |
| 17 | Sargable SQL / planned | 21 | Spring JDBC | PostgreSQL | boundary + query plan | Not run | planner/data dependent | Pending / Sol design |
| 18 | Keyset paging / planned | 21 | Spring JDBC | PostgreSQL | traversal + plan | Not run | stated snapshot semantics | Pending / Sol design |
| 19 | DynamoDB batching / planned | 21 | AWS SDK v2 | DynamoDB Local | request count + partials | Not run | no managed throttling/IAM | Pending / Sol design |
| 20 | Pool admission / planned | 21 | Spring JDBC, Hikari | PostgreSQL, Toxiproxy | forced saturation/loss | Not run | not RDS failover | Pending / Sol design |
| 21 | Deadline budget / planned | 21 | Spring MVC, HTTP clients | WireMock | manual-time budget | Not run | local HTTP only | Pending / Sol design |
| 22 | Retry/jitter / planned | 21 | Spring Boot, Resilience4j | WireMock | fake time/random | Not run | policy simulation | Pending / Sol design |
| 23 | HTTP idempotency / planned | 21 | Spring MVC/JDBC | PostgreSQL | concurrency + restart | Not run | remote effect excluded | Pending / Sol design |
| 24 | Circuit breaker / planned | 21 | Spring Boot, Resilience4j | none | transition matrix | Not run | per-instance core | Pending / Sol design |
| 25 | Bulkheads / planned | 21 | Spring Boot, Resilience4j | none | deterministic saturation | Not run | configured capacity only | Pending / Sol design |
| 26 | Fair rate limit / planned | 21 | Plain Java + Spring | optional Redis | virtual-time properties | Not run | local core; Redis limits | Pending / Sol design |
| 27 | Load shedding / planned | 21 | Spring MVC, Micrometer | none | queue/admission matrix | Not run | process-local | Pending / Sol design |
| 28 | Stale fallback / planned | 21 | Spring Boot, Caffeine | optional Redis | fake-clock matrix | Not run | bounded stale only | Pending / Sol design |
| 29 | Partial fan-out / planned | 21 | Spring MVC, HTTP clients | WireMock | controlled outcomes | Not run | no universal client winner | Pending / Sol design |
| 30 | Graceful shutdown / planned | 21 | Spring Boot, Actuator | none | lifecycle gates | Not run | local orchestrator model | Pending / Sol design |
| 31 | Transactional outbox / planned | 21 | Spring Boot/JDBC | PostgreSQL | seam transaction failures | Not run | atomic intent, not publish | Pending / Sol design |
| 32 | Outbox relay / planned | 21 | Spring Boot/JDBC, AWS SDK | PostgreSQL + SQS emulator | send-then-throw + restart | Not run | at-least-once | Pending / Sol design |
| 33 | Inbox dedupe / planned | 21 | Spring Boot/JDBC, AWS SDK | PostgreSQL + SQS emulator | duplicate/crash matrix | Not run | retention/replay policy | Pending / Sol design |
| 34 | Optimistic writes / planned | 21 | Spring Boot/JDBC | PostgreSQL; DynamoDB ext | coordinated writers | Not run | emulator conditional path | Pending / Sol design |
| 35 | Pessimistic locks / planned | 21 | Spring Boot/JDBC | PostgreSQL | real lock/deadlock | Not run | not multi-region | Pending / Sol design |
| 36 | Durable saga / planned | 21 | Spring Boot | PostgreSQL + queues | state-machine recovery | Not run | no distributed ACID | Pending / Sol design |
| 37 | Event ordering / planned | 21 | Spring Kafka | Kafka KRaft | controlled reorder + broker | Not run | single-node; per partition | Pending / Sol design |
| 38 | SQS lifecycle / planned | 21 | Spring Boot, AWS SDK | SQS emulator | visibility/redrive matrix | Not run | AWS scheduler not proven | Pending / Sol design |
| 39 | Event contracts / planned | 21 | Spring Boot, Jackson | broker/emulator | golden consumer/routing | Not run | provider routing differs | Pending / Sol design |
| 40 | S3/cache consistency / planned | 21 | Spring Boot, AWS SDK | S3/SQS emulator | stale resurrection/events | Not run | AWS notifications not proven | Pending / Sol design |
| 41 | Stateless scale / planned | 21 | Spring MVC | PostgreSQL/Redis | two-instance routing | Not run | local replicas | Pending / Terra design |
| 42 | Hot partitions / planned | 21 | AWS SDK, jqwik | DynamoDB Local | distribution + conflict | Not run | no fleet throttling | Pending / Terra design |
| 43 | Cache stampede / planned | 21 | Caffeine | optional Redis | barrier-controlled load | Not run | core process-local | Pending / Terra design |
| 44 | CQRS projection / planned | 21 | Spring Boot | PostgreSQL + broker | replay + restart | Not run | eventual contract explicit | Pending / Terra design |
| 45 | Leases/fencing / planned | 21 | Spring Boot | PostgreSQL/Redis | stale-write rejection | Not run | partition model bounded | Pending / Terra design |
| 46 | Structured logs / planned | 21 | Spring Boot, Micrometer | none | sink + redaction | Not run | local log pipeline | Pending / Terra design |
| 47 | SLI/SLO metrics / planned | 21 | Micrometer | none | meter + rule fixtures | Not run | no production traffic | Pending / Terra design |
| 48 | Async tracing / planned | 21 | Spring, OpenTelemetry | optional collector | in-memory span graph | Not run | backend behavior excluded | Pending / Terra design |
| 49 | Tenant isolation / planned | 21 | Spring Security | PostgreSQL + S3 adapter | adversarial matrix | Not run | IAM fidelity excluded | Pending / Terra design |
| 50 | Cloud capstone / planned | 21 | Two Spring services | PostgreSQL + SQS/S3 emulators | end-to-end failure matrix | Not run | no real AWS/managed failover | Pending / Terra design |
