# Virtual Threads with a Downstream Bulkhead

- **Category:** Virtual threads, resilience, and downstream protection
- **Difficulty:** Advanced
- **Estimated time:** 75 minutes

## Scenario, symptoms, and business impact

A service creates a virtual thread for every blocking request to a downstream
client. Virtual threads make waiting callers inexpensive, but the downstream
dependency still has a finite connection, session, or concurrency budget. The
starter lets every caller cross that boundary at once. A burst can exhaust the
dependency, increase tail latency for unrelated traffic, and turn a recoverable
slowdown into cascading failure.

The required bulkhead protects the scarce downstream resource, not the number of
virtual threads the application can create.

## Learning objectives and prerequisites

- Separate application thread scalability from downstream capacity.
- Enforce a local in-flight-call invariant with interruptible acquisition.
- Release capacity on successful, failed, and interrupted downstream calls.
- State the limits of a process-local bulkhead.

Complete exercises 01--07 first. You should be comfortable with
`InterruptedException`, `try`/`finally`, and Java 21 virtual threads.

## Underlying cause without revealing the solution

Unlimited cheap callers do not imply unlimited expensive work. The starter
stores a maximum but does not apply it at the dependency boundary, so it has no
admission control over concurrent `DownstreamClient.fetch` calls. A solution
must also make the capacity accounting survive every exit path.

## System invariants

- `maximumInFlight` is positive.
- At most `maximumInFlight` calls have entered `DownstreamClient.fetch` through a
  single gateway instance at any time.
- Every successful capacity acquisition is matched by exactly one release, even
  when the downstream client throws.
- If waiting for capacity is interrupted, the call propagates
  `InterruptedException` without entering the downstream client or releasing an
  unacquired permit.
- The gateway returns the downstream value or propagates the downstream failure;
  it does not invent a fallback.

## Delivery and consistency guarantees

`fetch` is a synchronous, in-memory concurrency boundary. It provides no
delivery, retry, timeout, caching, circuit-breaker, rate-limit, or request
cancellation guarantee. The in-flight bound constrains calls that enter this
gateway's supplied client; it does not prove that an interrupted remote request
stopped, nor that a returned value was durably committed anywhere.

## Process-local versus distributed guarantees

Each `BulkheadGateway` owns a separate budget. Multiple gateway instances, JVMs,
or pods each allow their own maximum, so the aggregate can still exceed a shared
downstream pool. Use a shared client pool, distributed admission service, or
downstream-enforced quota only when the product requires a cross-instance
contract.

## Functional requirements

- Preserve the `DownstreamClient` functional interface and synchronous `fetch`
  method.
- Reject a non-positive maximum with `IllegalArgumentException`.
- With a maximum of two, allow no more than two controlled calls to enter the
  downstream client while four virtual-thread callers are active.
- Return permits after both normal downstream completion and downstream failure.
- Preserve interruption rather than converting it to a successful result.

## Non-functional requirements and resource limits

The gateway holds one local concurrency primitive and no request queue of its
own. Callers waiting for capacity are still retained by their executing virtual
threads, so an upstream admission policy may be needed to bound waiting memory
and latency. The default policy need not be fair; the exercise requires a hard
concurrency cap, not per-request ordering. No latency or throughput target is
claimed.

## Constraints and forbidden shortcuts

- Do not treat virtual threads as a downstream capacity mechanism.
- Do not serialize every call with `synchronized`; that enforces a one-call cap
  rather than the configured budget and can unnecessarily hold a monitor across
  blocking I/O.
- Do not swallow `InterruptedException` or release capacity that was not
  acquired.
- Do not use sleeps, elapsed time, or CPU count to prove the limit.
- Do not replace this local bulkhead with a distributed-rate-limit claim.

## Architecture or sequence diagram

```text
virtual-thread caller --> acquire one local permit --> downstream.fetch --> release permit
                                  |                        |
                                  | no permit               +-- normal or exceptional exit
                                  v
                             wait interruptibly
```

## Deterministic failure reproduction

`BulkheadGatewayAcceptanceTest` runs four virtual-thread callers against a
downstream emulator. The emulator increments an in-flight counter, records the
maximum, and blocks the first two entries on a latch. The test observes exactly
two entries before release and verifies that all callers later complete. It uses
state gates and counters; its timeouts are liveness bounds rather than a capacity
measurement.

## Task and automated acceptance criteria

Implement the gateway so the controlled two-call budget is never exceeded and
capacity is reusable after calls complete. The acceptance suite also requires a
clear failure for a zero budget. Keep the starter compilable and its defect
behavioral: the learner should change the capacity boundary, not test plumbing.

## Exact build, test, run, and benchmark commands

Run these commands from the repository root:

```shell
./gradlew :exercises:08-virtual-threads-downstream-bulkhead:starter:compileJava
./gradlew :exercises:08-virtual-threads-downstream-bulkhead:starter:starterAcceptanceTest
./gradlew :exercises:08-virtual-threads-downstream-bulkhead:solution:test
./gradlew verifyExercise08
```

This exercise has no standalone application or benchmark. For a diagnostic
experiment on Java 21, add `-Djdk.tracePinnedThreads=full` to a representative
run; pinning diagnostics do not replace downstream capacity testing.

## Progressive hints: observation, mechanism, design direction

1. Count only calls that have crossed into `DownstreamClient.fetch`.
2. Make one unit of capacity correspond to one such in-flight call.
3. Acquire capacity before the call, and put release in a `finally` block whose
   scope starts only after successful acquisition.

## Common wrong solutions and why they fail

- `newVirtualThreadPerTaskExecutor()` scales callers but places no limit on the
  dependency.
- A monitor around `fetch` caps work at one regardless of the configured value.
- Releasing only after a normal return leaks capacity when the client fails.
- Catching interruption and proceeding violates cancellation and may oversubscribe
  the dependency.
- A local counter without an atomic acquisition race can allow too many callers
  through.

## Production and emulator boundaries

The controlled `DownstreamClient` is not a network transport, connection pool,
HTTP cancellation implementation, authentication layer, or shared quota. A
production client needs deadlines, transport-specific abort handling, connection
pool settings, retries that respect idempotency, error classification, load
shedding before virtual-thread accumulation, and a decision about capacity across
instances.

## Specialist extension

Compare immediate rejection (`tryAcquire`) with a deadline-bounded wait and
document the resulting API contract. Instrument permit wait time, downstream
duration, in-flight calls, interruption, and timeout/rejection outcomes. Then
combine the bulkhead with a connection pool and a real per-tenant admission
policy, ensuring their limits cannot deadlock one another.

## Interview follow-up questions

- Why do virtual threads not eliminate a database or HTTP client pool limit?
- What changes if callers should receive an immediate overload response instead
  of waiting?
- How can a cancelled `CompletableFuture` or HTTP request still consume remote
  capacity?
- Where would you enforce a global, rather than per-pod, concurrency limit?

## Reflection and future-post evidence

Capture the controlled maximum-in-flight counter before and after the repair.
Explain why the permit follows the downstream call instead of the virtual-thread
lifecycle, and distinguish concurrency caps, rate limits, and thread counts. A
strong post names the local boundary and lists the transport and multi-instance
policies still required in production.

## Primary references

- [Semaphore API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)
- [Virtual threads, Java 21](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
