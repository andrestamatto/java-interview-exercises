# Troubleshooting

## The starter acceptance task fails

That is expected before the learner changes the starter implementation. Run
the normal starter `check` task first; use `starterAcceptanceTest` only to see
the named behavioral defect. Do not disable or retag the acceptance test.

## Gradle reports an unsupported Java version or cannot find Java 21

Run `./gradlew --version` and check the toolchain section. Install Eclipse
Temurin 21, configure `JAVA_HOME` for the shell if necessary, then restart the
Gradle daemon with `./gradlew --stop`. The core source and target release stay
on Java 21.

## Spotless fails when Gradle runs on Java 25

The build pins a `google-java-format` release that supports the Java 25
launcher. Update the wrapper/build dependencies together rather than lowering
the exercise source level. Run `./gradlew --no-daemon formatCheck` to diagnose
the launcher path without reusing an old daemon.

## A Docker integration test cannot start

Confirm that Docker Desktop/Engine is running and that the current user can
run `docker ps`. Run only the module's explicit `integrationTest` task. The
emulators prove the documented local contract, not IAM, managed failover,
regional latency, production quotas, or real-cloud billing behavior.

## A JMH result changes between runs

That is normal. JMH is evidence, not a fixed pass/fail score. Close unrelated
load, record JVM and machine details, use the supplied warmup/fork settings,
and compare distributions and GC allocation. Do not replace a JMH benchmark
with `System.nanoTime()` loops.

## Dependency locks changed unexpectedly

Use the checked-in wrapper and regenerate locks deliberately with the affected
module's documented Gradle command. Review the resolved dependency graph and
license impact before keeping the new lockfile. Never delete locks merely to
make a build pass.
