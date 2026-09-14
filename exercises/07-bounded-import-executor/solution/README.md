# Reference Solution: Bounded Import Executor

## Design and invariant rationale

The `ThreadPoolExecutor` has core and maximum pool sizes of two, a one-element
`ArrayBlockingQueue`, and `AbortPolicy`. Its normal admission path first uses the
two workers, then the one queue slot, then throws `RejectedExecutionException`.
`submit` catches only that exception and maps it to `false`; it lets a null task
fail fast through `Objects.requireNonNull`. This makes overload visible without
retaining an unbounded backlog or changing the caller into a worker.

## Resource use and complexity

The executor owns at most two active worker threads and one queued `Runnable`.
Bounded queue insertion is constant-time for this configuration; task execution
time and captured-memory size are outside the executor's control. The class
does not expose an executor queue, add a scheduler, or allocate a completion
future for each task.

## Failure and recovery behavior

Saturation and submission after executor shutdown are both represented as
`false`, because both surface as `RejectedExecutionException`. A task accepted
with `true` can still fail later, and that failure is not reported through the
boolean result. `close` calls `shutdownNow`: it requests interruption of active
workers and returns queued-but-not-started tasks, whose returned list this small
wrapper discards. It neither waits for termination nor proves interruption
stopped a task. Callers need a durable retry or dead-letter policy outside this
class.

## Alternatives and rejected designs

`CallerRunsPolicy` can be useful when deliberately slowing a known safe producer
is the contract, but it violates this exercise's explicit rejection result.
`SynchronousQueue` would permit no waiting work, which is a different capacity
budget. A message broker or database-backed command table gives durability and
recovery but costs another operational component; it is not a replacement for
stating local execution capacity.

## Multi-instance implications

The limit applies per executor instance only. N instances admit up to 3N local
tasks before rejection, so downstream capacity and tenant fairness remain
unprotected. A distributed quota, durable queue consumer group, or database
admission record must be designed separately when the contract spans instances.

## Observability

The wrapper does not emit metrics. At the admission boundary, record attempted,
accepted, rejected, active, queued, completed, failed, and interrupted task
counts, plus queue wait and execution latency. Avoid treating executor counters
as a delivery ledger: correlate them with durable import identifiers if the
business process needs auditability.

## Security considerations

`Runnable` is executable in-process code, not an authorization boundary. A real
endpoint must authenticate and authorize imports before consuming a scarce slot,
enforce input-size and tenant quotas, isolate file processing, and avoid logging
sensitive payloads on rejection. This class validates only a null task; it does
not validate task origin, contents, or resource cost.

## Limits

The queue is non-fair by default, there is no timed admission, and no graceful
drain or await-termination API. Rejection has no reason code, task result, or
retry delay. `shutdownNow` can leave non-cooperative work running. The bounded
queue protects this process's retained work, not external storage, CPU, or a
shared downstream service.

## Primary references

- [ThreadPoolExecutor API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ThreadPoolExecutor.html)
- [ArrayBlockingQueue API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ArrayBlockingQueue.html)
- [ExecutorService API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/ExecutorService.html)
