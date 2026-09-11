# Standard Exercise Contract

## Directory shape

```text
exercises/NN-behavior-oriented-name/
├── README.md
├── starter/
│   ├── build.gradle.kts
│   └── src/
├── solution/
│   ├── build.gradle.kts
│   └── src/
├── acceptance-tests/       # only when an external black-box contract helps
├── benchmark/              # only for genuine JMH microbenchmarks
└── docs/                   # only useful diagrams and primary-source links
```

Starter and solution are independently buildable. Starter dependencies may
never resolve solution output. Shared modules contain testing mechanisms, not
domain answers.

## README headings

1. Title, category, difficulty, and estimated time
2. Scenario, symptoms, and business impact
3. Learning objectives and prerequisites
4. Underlying cause without revealing the solution
5. System invariants
6. Delivery and consistency guarantees
7. Process-local versus distributed guarantees
8. Functional requirements
9. Non-functional requirements and resource limits
10. Constraints and forbidden shortcuts
11. Architecture or sequence diagram when it improves understanding
12. Deterministic failure reproduction
13. Task and automated acceptance criteria
14. Exact build, test, run, and benchmark commands
15. Progressive hints: observation, mechanism, design direction
16. Common wrong solutions and why they fail
17. Production and emulator boundaries
18. Specialist extension
19. Interview follow-up questions
20. Reflection and future-post evidence
21. Primary references

## Starter contract

- Always compiles in the supported baseline build. Intentional defects are
  behavioral and exposed by explicit acceptance tasks.
- Exposes one deliberate behavioral defect with a deterministic reproducer.
- Contains no misleading comments or hidden secondary failures.
- Does not reveal the final design through unused solution scaffolding.
- Keeps acceptance tests outside the default root build when failure is expected.

## Solution contract

- Preserves all stated invariants and semantics.
- Contains no TODO, placeholder, dead code, or unexplained suppression.
- Uses cohesive domain names and the smallest useful abstraction.
- Includes unit, integration, property, stress, or benchmark evidence appropriate
  to the claim—not every test style by default.
- Documents failure/recovery paths, operational signals, alternatives, and limits.
- Explains why the solution works, including complexity/resource use where
  relevant, rejected alternatives, multi-instance implications, security
  implications, and primary-source links.

## Completion gate

An exercise advances to `verified` only after formatting/static checks, starter
compilation, intended starter-failure evidence, solution tests, isolation checks,
and applicable integration/JMH smoke tests are recorded. `Audited` additionally
requires senior review of claims, README/code agreement, security, observability,
and emulator boundaries.
