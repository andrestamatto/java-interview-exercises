# Bounded Import Executor

- **Category:** Concurrency, overload protection, and resilience
- **Difficulty:** Advanced
- **Estimated time:** 75 minutes

## Scenario, symptoms, and business impact

An import endpoint receives a burst of expensive file-processing tasks. The
starter uses a fixed worker pool whose default work queue has no practical
admission limit. Each accepted task retains its `Runnable` and whatever that
task captures. Sustained overload therefore converts input traffic into heap
growth and delayed work instead of an explicit, actionable admission decision.

The business impact is an outage that starts as invisible latency: users wait
behind stale imports, memory pressure increases, and recovery takes longer
because the process retains work it can no longer serve promptly.

## Learning objectives and prerequisites

- Treat executor configuration as a resource and failure-policy contract.
- Distinguish admission from completion or durable delivery.
- Make saturation observable to the caller rather than retaining it silently.
- Reason about shutdown interruption without promising task cancellation.

Complete exercises 01--06 first. Familiarity with `ExecutorService`, queues,
and cooperative interruption is useful.

## Underlying cause without revealing the solution

Worker count alone does not bound an executor. A fixed pool can still accept
arbitrarily many queued tasks. If no rejection policy is reached, the caller
receives apparent success while heap usage and queueing delay absorb the burst.
The API also needs a clear meaning for an unsuccessful admission.

## System invariants

- At most two submitted imports execute at the same time.
- At most one additional admitted import waits in the executor queue.
- A submission that cannot be admitted returns `false`; it is not silently
  queued elsewhere or run on the submitting thread.
- A `true` result means local admission only, not successful execution,
  persistence, or completion.
- `close` requests interruption of active work and removes queued work according
  to the executor's immediate shutdown policy; tasks must cooperate with
  interruption.

## Delivery and consistency guarantees

The class owns only an in-memory executor. It has at-most-local-admission
semantics: a `true` return says the executor accepted the `Runnable` at that
moment, while `false` says the executor rejected it. There is no acknowledgement
of task success, retry, ordering, persistence, exactly-once execution, or
recovery after a process crash. A task that throws has no result channel through
this boolean API.

## Process-local versus distributed guarantees

Every `ImportExecutor` instance has a separate two-worker/one-slot budget.
Multiple service instances multiply capacity and can still overwhelm a shared
database or object store. Global quotas, fair tenant limits, durable handoff,
and cross-node backpressure require a shared component or explicit upstream
policy; an executor queue cannot provide them.

## Functional requirements

- Preserve `ImportExecutor` as an `AutoCloseable` with `boolean submit(Runnable)`.
- Configure two workers and capacity for exactly one queued task.
- Return `true` for the two active tasks and the one queued task in the controlled
  acceptance scenario, then return `false` for the next submission.
- Reject a null task with `NullPointerException` rather than treating it as
  saturation.
- Preserve immediate shutdown through `close`.

## Non-functional requirements and resource limits

The executor budget is exactly two running tasks and one retained queued task,
excluding internal executor bookkeeping. Admission work is constant-time for the
configured bounded queue; execution cost belongs to the task. This exercise
does not bound how long a running task ignores interruption, provide fairness,
or bound task memory captured before submission.

## Constraints and forbidden shortcuts

- Do not use an unbounded queue, an unbounded cached pool, or an arbitrary
  queue-size increase to hide overload.
- Do not use `CallerRunsPolicy`; it changes `false` into unbounded caller-thread
  work and changes the API's admission semantics.
- Do not block indefinitely waiting to submit or create extra workers above two.
- Do not use elapsed-time assertions or sleeps to prove saturation.
- Do not turn every failure into `false`: malformed inputs and task failures are
  distinct from capacity rejection.

## Architecture or sequence diagram

```text
submit #1, #2 --> two workers running
submit #3     --> bounded queue (one slot)
submit #4     --> explicit rejection --> false

close()       --> interrupt running workers; discard queued work locally
```

## Deterministic failure reproduction

`ImportExecutorAcceptanceTest` submits two tasks that record that their workers
started and wait on a latch. Once both are running, it fills the sole queue slot
and submits one more task. The reproducer observes admission state through
latches and return values, not through queueing time or processor speed.

## Task and automated acceptance criteria

Replace the starter's unbounded admission behavior while preserving the public
API. The acceptance test requires the controlled sequence `true`, `true`,
`true`, then `false`. The normal starter build excludes the deliberately failing
acceptance tag; keep the defect behavioral and keep starter and solution builds
independent.

## Exact build, test, run, and benchmark commands

Run these commands from the repository root:

```shell
./gradlew :exercises:07-bounded-import-executor:starter:compileJava
./gradlew :exercises:07-bounded-import-executor:starter:starterAcceptanceTest
./gradlew :exercises:07-bounded-import-executor:solution:test
./gradlew verifyExercise07
```

This exercise provides no standalone application or benchmark. Capacity and
queueing measurements belong to an integration workload with real import task
costs and resource limits.

## Progressive hints: observation, mechanism, design direction

1. Identify how many tasks the starter retains after both workers are busy.
2. Choose a queue whose capacity expresses the one waiting-task budget.
3. Choose a rejection policy, then translate only its capacity decision into the
   method's boolean result.

## Common wrong solutions and why they fail

- `Executors.newFixedThreadPool(2)` still uses an unbounded queue.
- An oversized queue postpones the failure while retaining more stale work.
- `CallerRunsPolicy` slows callers but does not fulfil the required rejection
  contract and can block request-handling threads.
- Swallowing task exceptions as admission results confuses execution failure with
  backpressure.
- Assuming `shutdownNow` proves a task stopped ignores code that does not react
  to interruption.

## Production and emulator boundaries

The acceptance tasks are latch-controlled in-process `Runnable`s. They are not
file parsing, uploads, authentication, a durable queue, a retry system, or a
cross-node limiter. A production import path must define request response
behavior for `false` (for example, a retryable overload response), tenant
quotas, task idempotency, persistence before acknowledgement, cancellation,
deadlines, and post-crash recovery.

## Specialist extension

Introduce a small admission-result type that separates saturation from lifecycle
closure, then connect it to a durable import command store. Add gauges and
counters around queue occupancy, active workers, rejections, and task outcomes.
Compare immediate rejection, timed admission, and a durable broker only after
specifying user-visible latency and delivery requirements.

## Interview follow-up questions

- Why can a fixed-size executor still cause an out-of-memory failure?
- When is caller-runs backpressure preferable to immediate rejection?
- What must happen after a caller observes `true` but the process crashes?
- How would you keep one noisy tenant from consuming this whole local budget?

## Reflection and future-post evidence

Show the four submissions against the controlled two-worker/one-slot state and
explain why the fourth result is an admission decision, not an import outcome.
For a future post, distinguish bounded in-memory work from durable messaging and
include the recovery policy that a real system would need.

## Primary references

- [ThreadPoolExecutor API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)
- [ArrayBlockingQueue API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ArrayBlockingQueue.html)
- [ExecutorService API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
