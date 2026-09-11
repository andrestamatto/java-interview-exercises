# Propagate Request Context Across Async Boundaries

**Category:** Concurrency and observability  
**Difficulty:** Advanced  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

An order API submits work to a shared executor. The worker loses the tenant identifier used for authorization and correlation, or inherits a previous tenant from a reused thread.

## Learning objectives and prerequisites

Capture, install, and always remove request-scoped context around asynchronous work. Complete exercises 04 and 09 first.

## Underlying cause without revealing the solution

`ThreadLocal` belongs to a physical thread, not to a logical request. Executor workers are reused and do not automatically inherit a submitting thread's state.

## System invariants

- A wrapped task observes the tenant present when it was wrapped.
- A reused worker contains no tenant after the task completes or throws.
- The caller's context is not mutated.

## Delivery and consistency guarantees

The wrapper provides lexical cleanup for one in-process `Runnable`; it is not remote context propagation.

## Process-local versus distributed guarantees

Thread-local state is not authentication or tenant authorization. Validate and authorize identity again at every remote trust boundary.

## Functional requirements

Implement `ContextTask.wrap(Runnable)` without changing `RequestContext`'s public API. Preserve the task exception and clean the worker in every path.

## Non-functional requirements and resource limits

Do not create an executor per task. Capture and cleanup are constant-size work for this one context field.

## Constraints and forbidden shortcuts

Do not use `InheritableThreadLocal`, global mutable request state, sleeps, or tests that depend on executor timing.

## Architecture or sequence diagram

```text
submitter --capture--> wrapped task --worker--> install -> run -> finally remove
```

## Deterministic failure reproduction

Run the starter acceptance task. A one-thread executor makes loss and leakage observable without a race.

## Task and automated acceptance criteria

Acceptance tests prove propagation, worker reuse cleanup, exceptional cleanup, absent context, and caller preservation.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:10-async-request-context:starter:starterAcceptanceTest
./gradlew verifyExercise10
```

## Progressive hints: observation, mechanism, design direction

1. Inspect which thread owns `ThreadLocal` state.
2. Capture before submission, not inside the worker.
3. Install only while the delegate runs and remove it in `finally`.

## Common wrong solutions and why they fail

- Returning the original task loses context on a worker.
- Cleanup only after normal completion leaks after exceptions.
- `InheritableThreadLocal` does not fix reused pool threads.

## Production and emulator boundaries

Production systems normally carry multiple fields and need framework context propagation such as OpenTelemetry. Verify MDC, reactive context, and remote propagation separately.

## Specialist extension

Compare Java 21 `ThreadLocal` handling with Java 25 Scoped Values only in an isolated preview build and document lexical inheritance accurately.

## Interview follow-up questions

- Why is context propagation not authorization?
- Which cleanup failure appears only under thread reuse?
- How would cancellation affect a context-aware `Callable`?

## Reflection and future-post evidence

Capture the reused-worker failure, exceptional-cleanup proof, and the boundary between local diagnostic context and distributed identity.

## Primary references

- [ThreadLocal API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/ThreadLocal.html)
- [ExecutorService API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
