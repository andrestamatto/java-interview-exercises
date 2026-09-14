# CompletableFuture Task Lifetime

- **Category:** Asynchronous composition, deadlines, and cancellation ownership
- **Difficulty:** Advanced
- **Estimated time:** 95 minutes

## Scenario, symptoms, and business impact

A request starts two equivalent asynchronous lookups and needs the first usable
answer. A plain race can return a winner while its sibling continues consuming a
connection, CPU, or remote quota. It can also leave both lookups running forever
when neither succeeds. These orphaned tasks compound under load, making a
successful-looking fan-out path leak expensive work and making tail latency
unbounded.

The coordinator needs one parent lifetime for both branches and for its deadline.
That parent must decide success, all-branch failure, timeout, and caller
cancellation without blocking a request thread.

## Learning objectives and prerequisites

- Model child-future ownership rather than treating composition as automatic
  resource cleanup.
- Race normal results while allowing one failed branch to leave the other useful.
- Use an injected deadline seam for deterministic tests.
- Separate cooperative `CompletableFuture` cancellation from a confirmed remote
  abort.

Complete exercises 01--08 first. You should understand `CompletableFuture`,
exceptional completion, `Duration`, and the difference between a deadline and a
retry policy.

## Underlying cause without revealing the solution

Combinators describe how a derived future completes; they do not transfer
ownership of source work or arrange its cancellation. In particular, a
first-result composition does not by itself stop the losing lookup, cancel a
timer, wait for a sibling after a failure according to this exercise's policy,
or give tests control over wall-clock time. Those lifecycle decisions must be
represented explicitly.

## System invariants

- The coordinator has one positive, non-null `Duration` deadline for both
  branches.
- The first branch to complete the parent normally supplies the parent's value.
- One failed branch leaves the other branch eligible to complete normally.
- If both branches fail before another terminal outcome, the parent completes
  exceptionally with the first failure observed by the coordinator.
- Once the parent is terminal--normal, exceptional, timed out, or cancelled by
  its caller--the scheduled deadline and both child futures receive cancellation
  requests.
- Cancellation is cooperative: a child future becoming cancelled does not prove
  that its underlying transport stopped.

## Delivery and consistency guarantees

The result is an in-memory `CompletableFuture`, not a durable workflow. A normal
completion returns one branch value. An exceptional completion may represent a
deadline or dependency failure. No result implies no exactly-once remote call,
transactional fan-out, durable cancellation, retry, ordering, or persistence
guarantee. If success and deadline completion race, whichever first completes
the parent determines its local result.

## Process-local versus distributed guarantees

The coordinator can cancel only the two local `CompletableFuture` handles and
its injected local scheduling handle. Separate JVMs, retrying clients, remote
servers, and message consumers may continue work. Cross-instance deduplication,
global deadlines, distributed cancellation, and side-effect idempotency require
protocol and storage support outside this exercise.

## Functional requirements

- Construct `FanoutCoordinator` with a non-null `DeadlineScheduler`.
- Keep `first(AsyncLookup left, AsyncLookup right, Duration timeout)` asynchronous
  and return a `CompletableFuture<String>` without calling `get` or `join`.
- Reject a null, zero, or negative timeout with `IllegalArgumentException`.
- Return the first normally completed branch value; do not fail early merely
  because the other branch failed.
- If both branches fail, preserve the first failure observed by the coordinator.
- Schedule one shared deadline that completes the parent with `TimeoutException`.
- On every parent terminal path, request cancellation of both children and the
  scheduled deadline.

## Non-functional requirements and resource limits

The coordinator retains two child futures, one result future, one scheduled-task
handle, and a constant amount of failure state per call. It creates no executor
or blocking thread. Completion callbacks execute according to
`CompletableFuture`'s completion rules, so lifecycle work should stay small. The
exercise does not bound outstanding fan-outs before their deadlines; upstream
admission control is still necessary under overload.

## Constraints and forbidden shortcuts

- Do not block with `get`, `join`, latches, or sleeps.
- Do not use wall-clock waits in acceptance tests; use the injected
  `DeadlineScheduler` to fire the stored deadline action deterministically.
- Do not use a first-result combinator without explicitly owning child and timer
  cleanup.
- Do not treat a cancellation request as proof of remote cancellation or success.
- Do not hide one dependency failure when all useful branches have failed.
- Do not change the two-lookup API into a thread-pool or server exercise.

## Architecture or sequence diagram

```text
left lookup  ---- normal value ----+                     +--> cancel deadline
                                   +--> parent result ---+--> cancel left child
right lookup ---- normal value ----+                     +--> cancel right child

one failure --> keep sibling eligible
both failures / deadline / caller cancellation --> terminal parent --> same cleanup
```

## Deterministic failure reproduction

The acceptance suite uses manually controlled `CompletableFuture`s and a
`ManualDeadlineScheduler`. It can complete a winner, fail one branch before the
other succeeds, fail both branches in a known order, or invoke the stored
deadline action directly. The test observes future state and cancellation flags;
it does not sleep until a real timer happens to fire.

## Task and automated acceptance criteria

Implement the parent lifecycle while keeping the starter compilable. The
acceptance criteria cover losing-child cleanup after success, cancelling the
deadline after completion, keeping a viable sibling after one failure,
propagating the first recorded failure after both fail, deadline cleanup, and
positive-deadline validation. The starter acceptance task is intentionally where
the behavioral gap is exposed.

## Exact build, test, run, and benchmark commands

Run these commands from the repository root:

```shell
./gradlew :exercises:09-completable-future-task-lifetime:starter:compileJava
./gradlew :exercises:09-completable-future-task-lifetime:starter:starterAcceptanceTest
./gradlew :exercises:09-completable-future-task-lifetime:solution:test
./gradlew verifyExercise09
```

This exercise has no standalone application or benchmark. Its deterministic
scheduler is a test seam, not a production scheduling implementation.

## Progressive hints: observation, mechanism, design direction

1. List every child resource created for one parent request, including the
   deadline handle.
2. Separate a normal branch result from a branch failure; only the former can
   win immediately in this contract.
3. Make a single parent completion the owner of cleanup so success, both-failed,
   deadline, and caller-cancel paths converge.

## Common wrong solutions and why they fail

- `applyToEither` alone leaves the losing source future running and has no
  cleanup relationship with a timer.
- Failing the parent after the first branch exception throws away a sibling that
  could still succeed.
- Using two independent timeouts gives the branches different lifetime policies
  and makes cleanup harder to reason about.
- Cancelling only the apparent loser misses deadline, both-failure, and external
  parent-cancellation paths.
- Assuming `cancel(true)` interrupts arbitrary asynchronous I/O overstates what
  `CompletableFuture` can guarantee.

## Production and emulator boundaries

`ManualDeadlineScheduler` stores a runnable and fires it under test control. It
is not a timer wheel, scheduled executor, distributed deadline service, network
abort, observability system, or retry mechanism. Production adapters must map
future cancellation to each transport's cancellation API, propagate request
deadlines, define timeout retries only for idempotent operations, and clean up
connections even when a peer continues the remote work.

## Specialist extension

Add a production scheduler adapter with cancellation-race tests, then add a
third branch and typed failure classification without abandoning a single parent
owner. Instrument timeout budget remaining, branch outcome, cancellation request,
transport-abort acknowledgement, and orphaned-work completion. Compare this API
with structured concurrency only after specifying Java-version and cancellation
semantics.

## Interview follow-up questions

- Why is a completed `CompletableFuture` not evidence that an HTTP request
  stopped?
- What failure policy would you use if one branch is authoritative and the other
  is only a cache?
- How do you avoid retrying a timed-out operation that may have committed?
- What race exists between a successful completion and a deadline callback?

## Reflection and future-post evidence

Show the four terminal stories--winner, one failure then winner, both failures,
and deadline--using the manual scheduler rather than a timing screenshot.
Explain that the parent owns cleanup but cannot prove remote cancellation. A
useful post connects that distinction to idempotency keys, deadline propagation,
and transport observability.

## Primary references

- [CompletableFuture API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- [CompletionStage API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletionStage.html)
- [Future cancellation API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html#cancel(boolean))
