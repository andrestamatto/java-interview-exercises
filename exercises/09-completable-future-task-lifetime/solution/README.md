# Reference Solution: CompletableFuture Task Lifetime

## Design and invariant rationale

The solution starts both lookups and retains their futures. `observe` lets a
normal branch call `result.complete(value)`; the first successful completion of
that shared future wins. A branch failure is recorded rather than completing the
parent immediately, so the sibling remains eligible. When the failure count
reaches two, the coordinator completes the parent exceptionally with the first
failure it recorded, unwrapping `CompletionException` when it has a cause.

The injected `DeadlineScheduler` creates one shared deadline that tries to
complete the same parent with `TimeoutException`. `CompletableFuture.complete`
is a single terminal transition, so a normal result, both-failure result,
deadline, and caller cancellation race safely for parent ownership. A
`whenComplete` callback on that parent cancels the deadline handle and requests
`cancel(true)` on both children for every terminal outcome. Cancelling both is
safe: a branch already completed simply remains completed.

## Resource use and complexity

Each call retains two child futures, one result future, one scheduled-task
handle, an `AtomicInteger`, and an `AtomicReference`. Setup and each completion
callback perform constant-size bookkeeping; lookup and scheduler cost belong to
their adapters. The class creates no threads, queues, or blocking waits. A large
number of concurrently live parents still retains their child futures until a
terminal outcome, so it needs upstream admission and deadline sizing in a real
service.

## Failure and recovery behavior

A single failed child does not terminate the parent. When both observed children
fail, the first failure recorded by the coordinator becomes the exceptional
result. A fired deadline completes the parent with `TimeoutException`; a normal
value racing that action may win instead. Any terminal result invokes cleanup.

`start` converts a synchronous `RuntimeException` from `AsyncLookup.start()` or
a null returned future into an exceptionally completed child, allowing the other
branch to remain useful. It does not catch `Error`. If the scheduler itself
throws while registering the deadline, the method throws after child work has
started; this minimal API has no recovery hook for that integration failure.
Retries, idempotency keys, transport abort acknowledgement, and durable recovery
must be handled outside the coordinator.

## Alternatives and rejected designs

`applyToEither` is concise for a normal-result race but does not express this
exercise's all-failure, shared-deadline, and cleanup policy. `anyOf` loses the
typed value and still leaves source ownership to the caller. `orTimeout` uses a
library-managed time source and does not by itself cancel children, which makes
the injected deterministic scheduler more appropriate here. Structured
concurrency can model cancellation scopes in a different API/runtime design, but
it does not remove the need to define remote cancellation and idempotency.

## Multi-instance implications

Future cancellation is local. It may notify an in-process adapter, but it cannot
cancel a separate service, queue consumer, or retrying client unless their
protocol cooperates. Multi-instance systems need propagated deadlines,
idempotency/deduplication, and an explicit remote-cancellation contract; local
parent cleanup is still useful to stop retaining local work.

## Observability

The coordinator emits no telemetry. At its boundary, capture parent outcome
(winner, both failed, timeout, caller cancellation), branch latency and failure
class, deadline registration/cancellation, cancellation requests, and any later
transport-abort acknowledgement. Use correlation IDs and error categories rather
than logging response bodies or credentials.

## Security considerations

`AsyncLookup` implementations define authentication, authorization, request
validation, and transport security; this class provides none. A cancellation
request must not be treated as revocation of a remote action or credential.
Protect tenant-specific deadline budgets, avoid exposing exception internals to
untrusted callers, and ensure retries do not duplicate privileged side effects.

## Limits

`CompletableFuture.cancel(true)` is cooperative; in this implementation its
interrupt flag does not itself control arbitrary running work. The deadline
scheduler contract does not eliminate a race with an action already firing.
There is no result cache, fallback, retry, cancellation reason, executor choice,
fairness, global quota, or durable workflow state. "First failure" means the
first failure recorded by competing completion callbacks, not a globally
observable timestamp across systems.

## Primary references

- [CompletableFuture API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- [CompletionStage API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/CompletionStage.html)
- [Future cancellation API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Future.html#cancel(boolean))
