# Phase 1 — Curriculum Design Review

## Outcome

The legacy prototypes were removed in PR #1 and remain recoverable from Git
history. Uncommitted work that existed before the rebuild is preserved in the
named stash `backup before curriculum rebuild`. The current branch,
`docs/phase-1-curriculum-design`, starts from the merged `develop` commit
`f1d60ec`.

Phase 1 adds design documents only. It does not create exercise code, build
logic, CI, containers, or a license.

## Safe Phase 2 migration

1. Recheck the branch and worktree before writing.
2. Preserve the authoritative prompt and Phase 1 documents.
3. Create root Gradle settings, version catalog, Wrapper, and convention plugins.
4. Add CI and shared test infrastructure without domain answers.
5. Implement only exercise 01 as the representative exercise.
6. Execute its complete quality gate and request the one required format review.
7. After approval, proceed in autonomous batches on a dedicated branch per batch.

The ignored `.idea/` and `out/` directories are local artifacts and must not be
committed. Their local deletion is unnecessary for repository cleanliness.

## Decisions already resolved

- Java 21 is the core baseline; Java 25 is isolated and optional.
- Spring Boot is used extensively but not in low-level JVM lessons.
- AWS scenarios require no paid cloud resources.
- Infrastructure tests are separated from the fast feedback loop.
- Apache-2.0 is the intended repository license.
- The owner writes the initial LinkedIn drafts; repository generation does not.
- Phase 3 continues autonomously after the representative exercise review.
- Git history uses small Conventional Commits and reviewed PRs.

## Decisions approved for Phase 2

1. **Local AWS emulation:** current LocalStack images require a
   `LOCALSTACK_AUTH_TOKEN` as of March 23, 2026. Recommended default: use
   ElasticMQ for SQS, MinIO for S3, and DynamoDB Local for DynamoDB, with a
   separately enabled LocalStack profile for learners who provide a token. The
   suite therefore requires accurate AWS-compatible local integration, not a
   credentialed LocalStack dependency.
2. **Spring generation:** Spring Boot 4.0.8/Spring Framework 7 is the pinned
stable baseline for new code. Recheck the official compatibility matrix before
moving to a newer Spring line or pinning a Spring Cloud release train; document
Boot 3/OpenFeign migration concerns separately.
3. **Build runtime:** use the latest stable Gradle 9.x release verified with both
   Java 21 and 25. The current compatibility matrix requires Gradle 9.1+ to run
   on Java 25. Use toolchains and `--release`, not only source compatibility.
4. **Capstone time box:** approve a 120-minute core path plus an optional
   120-minute specialist extension. A complete multi-service capstone cannot be
   honest inside a single 120-minute implementation without supplied scaffolding.
5. **Copyright notice:** confirm `Copyright 2026 André Stamatto` for Apache-2.0.
6. **CI Docker policy:** recommended PR-required Docker lane for one representative
   integration per affected exercise, with heavier recovery suites nightly/manual.
7. **Tool availability:** Eclipse Temurin 21.0.12.1+1 and 25.0.4.1+1 were
   installed locally. Gradle is supplied by the checked-in Wrapper. Docker is
   deferred until the first applicable infrastructure exercise.

## Design risks and mitigations

- JMM failures are not always reproducible on demand. Use deterministic contract
  gates plus jcstress evidence; never fake a stale read.
- A performance winner is not an acceptance criterion. Prove semantics first,
  then report JMH distributions and environment details.
- Emulator behavior is not managed-cloud fidelity. Every cloud exercise states
  its proof and non-proof boundary.
- "Exactly once" is prohibited across ordinary database/broker boundaries.
  Outbox/inbox designs state their at-least-once and idempotency contracts.
- Shared support may accidentally leak answers. Classpath isolation and an
  explicit architecture gate prevent starter-to-solution dependencies.
- Infrastructure-heavy exercises can overwhelm CI. Tags, reusable containers,
  bounded parallelism, and separate lanes preserve feedback speed.

## Skill influence

- Git workflow: recoverable cleanup, two logical commits, reviewed PR, and a
  fresh phase branch.
- Java clean code: small APIs, behavior-oriented names, no speculative layers.
- DDD: named invariants, value objects, aggregate boundaries, past-tense events.
- Event-driven architecture: stable envelopes, idempotency, replay, DLQ, and
  explicit delivery semantics.
- Spring Security: deny-by-default authorization, tenant isolation, token and
  data-leak negative tests.

## Primary references used in Phase 1

- [Spring Boot system requirements](https://docs.spring.io/spring-boot/system-requirements.html)
- [Spring REST clients](https://docs.spring.io/spring-framework/reference/integration/rest-clients.html)
- [Spring Cloud OpenFeign](https://docs.spring.io/spring-cloud-openfeign/reference/spring-cloud-openfeign.html)
- [Gradle Java compatibility](https://docs.gradle.org/current/userguide/compatibility.html)
- [Gradle toolchains](https://docs.gradle.org/current/userguide/toolchains.html)
- [Testcontainers LocalStack module](https://java.testcontainers.org/modules/localstack/)
- [GitHub Actions with Gradle](https://docs.github.com/en/actions/tutorials/build-and-test-code/java-with-gradle)
