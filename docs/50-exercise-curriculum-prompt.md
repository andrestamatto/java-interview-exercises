# Production-Grade Java Curriculum — Authoritative Implementation Prompt

> This is the single authoritative prompt for rebuilding this repository. Follow
> its phased process without silently weakening or omitting requirements.

## Role and mission

Act as a Principal Java Engineer, distributed-systems specialist, technical
interviewer, and curriculum designer. Rebuild `java-interview-exercises` as a
rigorous curriculum containing exactly 50 hands-on exercises for Senior Java
Software Engineer and Java Specialist Software Engineer preparation.

Teach the fundamental causes of production problems in robust, large-scale
enterprise systems—not merely frameworks, annotations, recipes, or trivia.
All code, tests, comments, documentation, commits, diagrams, and exercise
content must be written in English.

Preserve user changes. Before destructive work, inspect Git and present a safe,
recoverable migration plan. Never claim a test, benchmark, integration, or
command passed unless it actually ran.

## Primary objective

Every exercise must teach the learner to:

1. Recognize a realistic production problem.
2. Reproduce it deterministically.
3. Explain its computer-science or distributed-systems cause.
4. Identify the invariants that must be preserved.
5. Implement a production-quality solution.
6. Prove it through tests, benchmarks, or observable behavior.
7. Discuss alternatives, limitations, and trade-offs in a senior interview.

Keep exercises conceptually small, but never simplify away an essential failure
mode or make a misleading distributed-systems claim.

## Java versions

- Java 21 is the mandatory baseline.
- Every core exercise must build on Java 21 unless marked as a Java 25 extension.
- Add Java 25 extensions only when a feature addresses a real engineering pain.
- Isolate Java 25 code in a source set, subproject, or build profile so it never
  breaks the Java 21 build.
- Label preview features, use `--enable-preview`, and never present them as stable.
- Verify feature status in current official OpenJDK or Oracle documentation.
- Explain the pain solved by every newer feature; never include novelty alone.

Relevant comparisons include virtual versus platform threads, virtual threads
with downstream limits, `ThreadLocal` versus Scoped Values,
`CompletableFuture` versus Structured Concurrency, improved domain modeling,
and measurable runtime, GC, profiling, or security improvements. Java 21 virtual
threads belong in the core; preview Java 25 APIs belong in optional extensions.

## Technology baseline

Use Eclipse Temurin Java 21, Gradle 9.x Kotlin DSL, a checked-in Gradle Wrapper, centralized pinned
versions, JUnit 5, AssertJ, Awaitility, jqwik where useful, JMH exclusively for
microbenchmarks, and Testcontainers for real infrastructure integration.

Use PostgreSQL/MySQL, Kafka-compatible brokers, Redis, WireMock, Toxiproxy,
OpenTelemetry, Micrometer, ArchUnit, Resilience4j, and other libraries only when
they materially support an exercise. Use Docker Compose only when it improves
manual exploration beyond Testcontainers. Never require a paid service or cloud
account. Everything must run locally or in CI.

## Spring ecosystem

Approximately 25–35 exercises should use Spring Boot or another relevant Spring
project. Use plain Java where Spring would obscure JVM, memory-model,
concurrency, collection, or benchmarking mechanics.

Use Spring MVC, WebFlux where genuinely reactive, Data JPA, JDBC, Transactions,
Security, Retry or Resilience4j, Cloud CircuitBreaker, Cloud LoadBalancer,
OpenFeign, HTTP Service Clients with `@HttpExchange`, `RestClient`, `WebClient`,
Spring Kafka, Micrometer, Actuator, OpenTelemetry, and focused Spring tests where
appropriate.

Teach OpenFeign as an established declarative client and include its operational
pitfalls. Do not call it universally preferred or the newest approach. Include
a comparison or migration involving OpenFeign, `RestClient`, `WebClient`, and
HTTP Service Clients. Select clients based on blocking behavior, streaming,
timeouts, observability, load balancing, resilience, startup, and operational
complexity. Never introduce WebFlux just to appear modern.

## AWS and cloud

AWS is mandatory, with no paid account. Use local emulation, Testcontainers,
controlled fakes, or ports and adapters, and prefer AWS SDK for Java v2.

Cover:

- RDS concepts via PostgreSQL/MySQL Testcontainers: pools, transactions, locks,
  failover assumptions, and retry boundaries.
- SQS visibility, redelivery, idempotency, DLQs, long polling, and batching.
- S3 upload, multipart behavior, idempotent processing, metadata, checksums, and
  presigned URLs.
- DynamoDB conditional writes, optimistic concurrency, partitioning, and hot keys.
- SNS or EventBridge fan-out and contract evolution.
- IAM-style least privilege and safe credential handling conceptually.
- A capstone combining HTTP, RDS-like persistence, SQS, and S3.

Use credential-free AWS-compatible local services when accurate: ElasticMQ for
SQS, MinIO for S3, and DynamoDB Local for DynamoDB. LocalStack is an optional
profile only when a learner supplies its required token. Emulator-backed READMEs must state
what the emulator proves, what it cannot prove, what needs real-cloud validation,
and concerns such as IAM, quotas, regions, managed failover, networking, latency,
and cost. GCP/Azure may appear in comparisons; do not triple implementations.

At least one exercise must require Docker/Testcontainers; use them elsewhere
whenever realism justifies the complexity.

## Repository architecture

```text
java-interview-exercises/
├── README.md
├── LICENSE
├── settings.gradle.kts
├── build.gradle.kts
├── gradle/
├── docs/
│   ├── 50-exercise-curriculum-prompt.md
│   ├── curriculum.md
│   ├── prerequisites.md
│   ├── learning-path.md
│   ├── progress.md
│   ├── linkedin-series-guide.md
│   └── troubleshooting.md
├── shared/
│   ├── test-support/
│   └── observability-support/
└── exercises/
    ├── 01-meaningful-name/
    │   ├── README.md
    │   ├── starter/
    │   └── solution/
    └── ...
```

Each starter and solution must be independently executable. A starter must
never see its solution on compile, runtime, or test classpaths.

Provide root tasks equivalent to `verifySolutions`, `verifyExerciseXX`,
`runExerciseXX`, `benchmarkExerciseXX`, `integrationTest`, and
`dependencyUpdates`. Do not make root `build` permanently fail because starters
are intentionally defective; provide explicit starter acceptance-test tasks.

## Required exercise README

Every README must contain:

1. Title, category, difficulty, and estimated time.
2. Prerequisites and earlier-exercise dependencies.
3. A realistic incident, symptoms, and business impact.
4. The cause without revealing the full answer.
5. Explicit invariants and functional/non-functional requirements.
6. Constraints and forbidden shortcuts.
7. A diagram when relationships or sequences benefit from one.
8. Deterministic failure reproduction.
9. Clear implementation tasks and automated acceptance criteria.
10. Exact build, test, run, and benchmark commands.
11. Three levels of progressive hints.
12. Common incorrect solutions and why they fail.
13. Senior interview follow-ups.
14. Production considerations outside the exercise boundary.
15. A reflection checklist.

The separate solution must include complete production-quality code without
TODOs, invariant-focused tests, rationale, complexity/resource analysis,
failure and recovery analysis, rejected alternatives, applicability limits,
multi-instance implications, observability signals, security implications, and
primary-source links. Do not hide essential logic in test utilities.

## Exercise design rules

Every exercise must have one primary and at most two secondary objectives, and
normally take 45–120 minutes. Use deterministic tests; avoid `Thread.sleep` as
synchronization and flaky wall-clock assertions. Prefer barriers, latches, fake
clocks, controlled executors, or deterministic fault injection.

Use JMH instead of hand-written `System.nanoTime()` loops. Distinguish latency,
throughput, scalability, asymptotic complexity, and resource use. Distinguish
process-local from distributed guarantees. State delivery and consistency
semantics. Test unhappy paths, cancellation, timeout, partial failure, and
recovery. Never use an in-memory map to prove a distributed guarantee or claim
that retries, transactions, locks, caches, or brokers guarantee more than they
do. Prefer behavior-oriented names over `Manager`, `Helper`, and `Utils`.

Use Testcontainers where infrastructure is essential and plain Java where it
models the concept accurately.

## Exact 50-exercise curriculum

### Concurrency and Java Memory Model

1. Lost updates and atomicity.
2. Memory visibility and `volatile`.
3. Compound operations and atomic classes.
4. Safe publication and immutability.
5. Deadlock detection and lock ordering.
6. Lock contention and smaller critical sections.
7. Bounded executors and thread-pool exhaustion.
8. Virtual threads, pinning, and downstream concurrency limits.
9. `CompletableFuture` failure, cancellation, and timeout propagation, with an
   optional Java 25 Structured Concurrency comparison.
10. Context propagation across async boundaries, optionally with Scoped Values.

### Performance and data access

11. Accidental quadratic complexity and indexing.
12. Correct microbenchmarking with JMH.
13. Allocation, object lifetime, and GC pressure.
14. Boxing, primitive specialization, and data representation.
15. Choosing collections from access patterns.
16. Database N+1 queries with Spring Data JPA.
17. Indexes, query plans, and non-sargable predicates.
18. Offset versus keyset pagination.
19. Batching and excessive network round trips, including AWS SDK behavior.
20. Connection-pool saturation, backpressure, and RDS-like assumptions.

### Resilience and resource protection

21. End-to-end timeout budgets across HTTP calls.
22. Safe retries with exponential backoff and jitter.
23. Idempotency under retried HTTP requests.
24. Circuit breakers and recovery.
25. Bulkheads and failure isolation.
26. Rate limiting and fairness.
27. Load shedding and bounded queues.
28. Graceful degradation and stale-cache fallback.
29. Partial failure in fan-out calls and HTTP client selection.
30. Graceful shutdown and request draining.

### Data consistency and messaging

31. Dual writes and transactional outbox.
32. Reliable outbox publication and duplicate broker/SQS delivery.
33. Idempotent consumers and inbox deduplication.
34. Optimistic concurrency and conditional writes.
35. Pessimistic locking and contention.
36. Saga orchestration and compensation.
37. Event ordering and partition keys.
38. SQS visibility timeout, poison messages, and DLQs.
39. Backward-compatible event schemas with SNS, EventBridge, or Kafka.
40. Cache invalidation, S3 object events, and consistency boundaries.

### Scalability, architecture, observability, and security

41. Stateless services and horizontal scaling.
42. Partition skew, DynamoDB design, and hot keys.
43. Cache stampede and request coalescing.
44. CQRS and eventually consistent projections.
45. Distributed leases and fencing tokens.
46. Structured logging and correlation IDs.
47. Metrics, SLIs, SLOs, and alert design.
48. Distributed tracing across asynchronous work.
49. Multi-tenant authorization, IAM-style least privilege, and data isolation.
50. Capstone: compatible API/event evolution with HTTP, RDS, SQS, and S3.

Do not silently replace a topic. Explain and obtain approval for a curricular
change. At least ten exercises need a specialist extension involving fault
injection, contention, formal invariants, performance, coordination, recovery,
or comparable depth.

## Correctness and validation

Before declaring an exercise complete:

1. Run formatting and static analysis.
2. Compile starter and solution.
3. Run all solution tests.
4. Demonstrate the intended starter failure.
5. Confirm acceptance tests fail for the intended reason before the solution
   and pass afterward.
6. Confirm repeatability.
7. Run JMH for performance claims.
8. Run integration tests for infrastructure behavior.
9. Verify starter/solution isolation.
10. Review documentation against behavior.
11. Report exact commands and outcomes.

Use Spotless, Checkstyle/Error Prone, JaCoCo, and ArchUnit only for meaningful
feedback. Do not optimize for coverage percentage; prove invariants and failure
modes.

## Mandatory suite coverage

Include a plain-Java track, JMH, relational Testcontainers, AWS-compatible local emulators,
messaging, Redis or another cache, deterministic fault injection, logs/metrics/
traces, a multi-service capstone, an isolated Java 25 extension, and CI that
verifies Java 21 and Java 25 separately. Tag Docker tests separately. Full
benchmarks and heavy suites must use explicit tasks; keep default feedback fast.

## Interview quality

Every exercise should support a 45–60 minute senior interview. Questions must
probe concurrent correctness, multiple instances, failure/recovery, scaling,
operations, integrity, security, alternatives, and cost/complexity. Avoid trivia
and framework memorization.

## Social-media policy

Do not generate posts, engagement copy, screenshots, or 50 post templates.
Preserve factual material: problem, hypothesis, reproduction, evidence, results,
trade-offs, screenshot/diagram opportunities, and reflection questions.

The owner decides when to publish and normally supplies the first draft. When
asked later, improve it without replacing the owner's voice; never invent
experience, results, emotions, or lessons. Suggest screenshots but do not create
them unless asked. Prefer credibility over engagement bait. Create only a small
`docs/linkedin-series-guide.md`; an optional series title is
`50 Production-Grade Java Challenges — Exercise NN`.

## Licensing and originality

Prepare the repository for Apache License 2.0. Add the unmodified license,
appropriate owner copyright, `NOTICE` only when required, third-party notices
where required, and a dependency-license report. Do not add per-file headers
without reason. Never copy proprietary questions, paid material, certification
dumps, blog posts, or copyrighted examples. Write original scenarios, cite
primary sources, and respect dependency licenses.

## CI

Use GitHub Actions with pinned Java distributions, Wrapper validation, safe
Gradle caching, formatting/static checks, starter compilation, solution tests,
separate integration jobs, and Java 21/25 jobs. Normal PRs must not run full
JMH; run only a lightweight benchmark smoke test.

## Efficient execution and software-development skills

There is no arbitrary deadline. Use the minimum time and tokens that achieve an
excellent result; never trade correctness, reproducibility, or teaching quality
for speed. Gain efficiency through conventions, fixtures, reusable containers,
templates, small batches, independent parallel tasks, targeted tests, and
quality gates.

Use all locally available software-development skills when relevant. Read each
applicable skill completely before acting. In particular, apply:

- Git workflow guidance for branches, logical Conventional Commits, PRs, merge
  safety, and history review.
- Java clean-code guidance for implementations and reviews.
- DDD guidance for boundaries and models.
- Event-driven architecture guidance for messaging.
- Spring Security guidance for authentication, authorization, and isolation.

Skills inform execution but never override this prompt or owner instructions.

When agents/models are available, delegate only bounded independent tasks:

- Luna low/medium: mechanical scaffolding, formatting, repeated setup, and
  documentation/link checks.
- Terra medium/high: conventional Spring Boot, REST, persistence, ordinary
  tests, Testcontainers wiring, and repeated refactoring.
- Sol high/extra-high: Java Memory Model, concurrency, consistency, messaging,
  security, fault injection, JMH, and architecture review.
- Most capable available model: curriculum, shared conventions, capstones,
  global audits, and disputes between implementation and guarantees.

The lead remains accountable. Review and execute delegated work, avoid concurrent
edits to the same files, and never choose a weaker model for subtle correctness.
Record delegation and validation in `docs/progress.md`.

## Progress tracking

For each exercise, `docs/progress.md` must record status (planned, scaffolded,
implemented, verified, audited), concept, Java version, frameworks, infrastructure,
test type, verification commands/date, limitations, senior review, and delegated
work. Compilation alone is not completion; invariants and failure modes must be
verified.

## Phased implementation

### Phase 1 — Design only

Inspect repository and Git state; present a migration plan; produce the detailed
catalog, dependency graph, progression, standard template, shared Gradle design,
Docker/JMH mapping, resource estimates, and decisions needing approval. Do not
implement exercises in Phase 1.

### Phase 2 — Foundation and representative exercise

Create the Gradle foundation, Wrapper, version management, formatting, checks,
CI, justified shared support, and one complete representative exercise. Execute
and verify it, then stop once for owner review of structure, depth, and style.

### Phase 3 — Autonomous batches

After representative approval, continue autonomously. Use batches of five for
conventional independent work and three for concurrency, distributed systems,
security, cloud, or heavy infrastructure.

After every batch: compile changes, run targeted and relevant integration tests,
run benchmark smoke tests, review docs against behavior, update progress, record
commands/results, and fix failures before continuing. Commit only when explicitly
authorized. Do not request routine approvals.

Pause only for unapproved destructive work, missing credentials/authority, a
choice that materially changes learning goals, licensing/copyright risk, or a
blocker without a safe local alternative.

### Phase 4 — Final audit

Verify all solutions and cross-platform commands; audit isolation and READMEs;
remove accidental duplication; produce learning/interview indexes and a command
reference; report heavy resource requirements; and conduct a final senior
architecture/correctness review.

## Output expectations

Lead with outcomes, list changed files, provide reproduction commands, distinguish
executed from proposed checks, report failures honestly, synchronize docs and
code, preserve unrelated changes, and keep Git history semantic and reviewable.

## Initial execution instruction

Start with Phase 1 only. Produce:

1. Migration plan.
2. Detailed 50-exercise catalog.
3. Learning dependency graph.
4. Standard exercise structure.
5. Gradle architecture.
6. Testing and validation strategy.
7. Infrastructure and emulator boundaries.
8. Resource/time estimates.
9. Decisions requiring owner approval.

Do not implement curriculum code during Phase 1.
