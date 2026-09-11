# Java Interview Exercises

This repository is being rebuilt as a production-grade curriculum containing
50 hands-on exercises for senior Java engineering interviews and real-world
system design practice.

The authoritative implementation brief is available at
[`docs/50-exercise-curriculum-prompt.md`](docs/50-exercise-curriculum-prompt.md).

Phase 1 curriculum design and the Phase 2 representative implementation are
complete. The next batches build the remaining exercises from this reviewed
foundation.

The baseline is Java 21 on Eclipse Temurin. Java 25 extensions will remain
isolated, and Spring Boot 4.1.x is the default for framework-oriented exercises.

```shell
./gradlew formatCheck compileStarters verifySolutions verifyStarterIsolation
./gradlew verifyExercise01
```

Phase 1 design artifacts:

- [`docs/phase-1-plan.md`](docs/phase-1-plan.md)
- [`docs/curriculum.md`](docs/curriculum.md)
- [`docs/learning-path.md`](docs/learning-path.md)
- [`docs/exercise-template.md`](docs/exercise-template.md)
- [`docs/architecture.md`](docs/architecture.md)
- [`docs/progress.md`](docs/progress.md)

The project is licensed under the Apache License 2.0. Copyright 2026 André
Stamatto.
