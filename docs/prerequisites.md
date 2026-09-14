# Prerequisites

The core path uses Java 21 and the checked-in Gradle Wrapper. Do not install or
use Maven for this repository.

## Required for exercises 01–15

- Eclipse Temurin 21 (or another Java 21 distribution supported by Gradle).
- Git and a shell capable of running `./gradlew` or `gradlew.bat`.
- At least 4 GB of free disk space for Gradle caches and generated benchmark
  reports.

Verify the baseline before starting an exercise:

```shell
java -version
./gradlew --version
./gradlew formatCheck compileStarters verifySolutions verifyStarterIsolation
```

The compiler toolchain remains Java 21 even if the Gradle launcher uses Java
25. The build validates formatting with either supported launcher.

## Required only for marked extensions

- Java 25 for the explicitly isolated Java 25 extension source sets. Preview
  features are always marked and never required by the Java 21 core.
- Docker Desktop or a compatible Docker Engine for an exercise tagged
  `integration`. PostgreSQL, Redis, Kafka, DynamoDB Local, ElasticMQ, MinIO,
  and Toxiproxy run locally through Testcontainers or a supplied configuration;
  no paid cloud account is required.
- Approximately 12 GB free RAM and 15 GB free disk for the heavier container
  batches and capstone. Stop unrelated containers before running those suites.

## Before solving an exercise

Read its root README first. The `starter` module must compile but its explicit
`starterAcceptanceTest` is intentionally red until you implement the fix. The
`solution` module is a separate reference implementation; never add it as a
dependency of the starter.

Use the exact commands in the README. Infrastructure and full JMH tasks are
explicit because they take longer and make stronger environment assumptions.
