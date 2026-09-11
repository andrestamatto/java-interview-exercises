# Build, Testing, and Infrastructure Architecture

## Gradle design

- Root Kotlin DSL with `gradle/libs.versions.toml`, dependency locking, and a
  checked-in Wrapper.
- An included `build-logic` build supplies narrow convention plugins:
  `java-exercise`, `spring-exercise`, `integration-test`, `jmh-exercise`, and
  `java25-extension`.
- Java 21 toolchains and `options.release = 21` are the default. Java 25 tasks
  resolve their own toolchain and never enter the Java 21 dependency graph.
- Shared modules are limited to `shared:test-support` (manual clocks, barriers,
  fault plans, container helpers) and `shared:observability-support` (capturing
  exporters and redaction assertions).
- A `verifyStarterIsolation` task inspects resolved classpaths and fails on any
  starter-to-solution edge.
- Root lifecycle tasks: `verifySolutions`, `verifyExerciseNN`, `runExerciseNN`,
  `benchmarkExerciseNN`, `integrationTest`, `dependencyUpdates`, and explicit
  starter acceptance tasks.

Spring Boot 4.1.x is the proposed default because the current release supports
Java 21 and 25. Exact Spring Cloud and plugin versions are pinned only after
checking their official compatibility matrices during Phase 2.

## Validation lanes

| Lane | Trigger | Evidence |
|---|---|---|
| Fast PR | Every push/PR | Wrapper validation, formatting, static analysis, starter compilation, solution unit/contract tests, isolation, JMH smoke |
| Docker PR | Affected modules | Bounded PostgreSQL, broker, Redis, and AWS-compatible emulator integration tests |
| Java 25 | Affected modules | Isolated compile/tests; preview flags only where official status requires |
| Nightly/manual | Scheduled/on demand | Repeated recovery suites, full JMH, dependency/license reports, capstone |

Actions must be pinned to immutable commit SHAs. Gradle caching uses the official
Gradle action. Full benchmarks never gate ordinary pull requests.

CI uses Eclipse Temurin for Java 21 and 25. Phase 2 pins exact current security
patch versions after verifying availability, records them in one version source,
and upgrades them intentionally rather than resolving `latest`.

## Test selection

- Unit/model tests prove pure invariants and state transitions.
- jqwik compares stateful implementations to a small reference model.
- Barriers, latches, manual tickers, and controlled executors force concurrency
  interleavings without sleeps.
- jcstress demonstrates JMM-allowed outcomes but is not the only deterministic
  acceptance gate.
- Integration tests prove database, broker, protocol, and SDK wiring.
- Send-then-throw adapters model ambiguous acknowledgement more precisely than a
  random network outage.
- JMH measures only in-process micro-operations. Database plans, request counts,
  queue depth, and load tests are used for system-level claims.

## Infrastructure matrix

| Capability | Exercises | Local implementation | What it cannot prove |
|---|---:|---|---|
| PostgreSQL/RDS-like SQL | 16–18, 20, 23, 31–36, 44–45, 49–50 | PostgreSQL Testcontainer | Managed failover, backups, RDS Proxy, regional latency |
| SQS | 32–33, 38–40, 50 | Proposed ElasticMQ; optional LocalStack profile | Full AWS quotas, IAM, scheduler timing, regional behavior |
| S3 | 40, 49–50 | Proposed MinIO plus synthetic AWS event fixtures; optional LocalStack | Exact AWS notifications, IAM, regional durability |
| DynamoDB | 19, 34, 42 | DynamoDB Local; scripted partial/unprocessed responses | Managed throttling fidelity, IAM, production partition fleet |
| Kafka | 37, 39, 44 | Single-node KRaft Testcontainer | Replica/ISR/controller/multi-AZ failure |
| Redis | 26, 28, 41, 43, 45 | Redis Testcontainer | Universal correctness under network partitions |
| Network faults | 20–30, 48, 50 | WireMock/Toxiproxy/controlled adapters | Internet topology and managed-service behavior |
| Observability | 46–50 | In-memory OTel exporter; optional collector | Vendor retention, query, and billing behavior |

LocalStack changed its current image policy to require an authentication token.
Until the owner decides otherwise, it is optional and must never make the suite
depend on an undisclosed credential.

AWS feature coverage is explicit: exercise 20 models connection loss and RDS
retry boundaries; 38 covers SQS long polling; 40 covers S3 multipart completion
and checksums; 49 covers presigned URL authorization; 50 integrates these
contracts without claiming managed-cloud fidelity.

## Resource estimates

- Core learner time: roughly 80–95 hours for 49 exercises plus a 2-hour capstone
  core; optional specialist extensions add roughly 25–35 hours.
- Implementation effort: approximately 15–25 focused engineering days after the
  representative template is approved, depending on CI/container debugging.
- Repository target: 150–300 MB source/history; container images and Gradle caches
  are external and may consume 8–15 GB locally.
- Comfortable workstation: 6 logical CPUs, 12 GB free RAM, 15 GB free disk.
- Heavy capstone: 4–6 processes/containers, roughly 4 GB additional RAM,
  5–10 minutes cold and 1–3 minutes warm.
- Current machine readiness observed in Phase 1: Java, Gradle, and Docker are not
  available on `PATH`; actual installed IDE runtimes were not treated as proof.
