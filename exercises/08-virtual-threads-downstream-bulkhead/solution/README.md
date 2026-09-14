# Reference Solution: Virtual Threads with a Downstream Bulkhead

## Design and invariant rationale

The constructor rejects a non-positive budget and creates one `Semaphore` with
the requested number of permits. `fetch` acquires a permit before crossing into
`DownstreamClient.fetch`; therefore, at most that many calls can be inside the
client at once. The `try` block begins after `acquire` returns, so `finally`
releases exactly the permits actually acquired. It runs for normal returns and
for failures from the downstream client.

`Semaphore.acquire()` is interruptible. If the caller is interrupted while
waiting, it exits before the `try` block, without a downstream call or a release.
The method's existing `InterruptedException` contract makes that signal visible
to its caller.

## Resource use and complexity

The gateway stores one semaphore and a downstream reference. Acquisition and
release are constant-time coordination operations in the uncontended case; a
waiting caller is queued by the semaphore's implementation. The code creates no
executor or request buffer, but callers blocked for permits can still accumulate
outside this class. Virtual threads reduce the cost of those blocked callers;
they do not remove their memory, deadline, or traffic-management cost.

## Failure and recovery behavior

Any exception or error escaping `downstream.fetch` still triggers permit release
and then propagates. Interrupt while waiting is propagated without admission.
There is no deadline, retry, fallback, or transport abort in this class. A remote
operation might continue after its caller is interrupted unless the downstream
adapter maps interruption or cancellation to the protocol's own abort mechanism.

## Alternatives and rejected designs

`tryAcquire` can support immediate rejection or a bounded wait, but it needs a
different return/error contract. A bounded executor controls task execution and
queueing, not necessarily the point where a shared downstream client consumes a
connection. `synchronized` is both stricter than the configured capacity and a
poor fit around blocking I/O. A rate limiter restricts work over time, which is
different from limiting concurrent in-flight operations.

## Multi-instance implications

The semaphore is local to one gateway object. A fleet of instances has one
budget per instance, so a shared downstream limit remains unprotected unless the
deployment coordinates it elsewhere. Conversely, local bulkheads are often
valuable even when the downstream also protects itself: they contain one
instance's contribution to overload.

## Observability

The class intentionally emits no telemetry. Instrument the boundary with current
in-flight calls, available capacity, permit wait duration, downstream duration,
interruption, failures, and rejected/timed-out admissions if those policies are
added. Use request correlation that avoids exposing sensitive request payloads.

## Security considerations

This is a capacity control, not an authorization or tenant-isolation mechanism.
Authenticate and enforce per-tenant quotas before scarce capacity is consumed,
protect downstream credentials, and avoid logging request contents. The gateway
does not validate the request string or implement a network security policy.

## Limits

The semaphore uses its default non-fair policy. There is no close lifecycle,
deadline, maximum queue length for waiting callers, distributed coordination, or
guarantee that cancellation stops remote work. The acceptance test proves a
local in-flight bound against an emulator; it does not measure production
throughput or connection-pool behavior.

## Primary references

- [Semaphore API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/Semaphore.html)
- [Virtual threads, Java 21](https://docs.oracle.com/en/java/javase/21/core/virtual-threads.html)
- [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
