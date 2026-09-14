# Reference Solution: Reduce Receipt Lock Contention

## Design and invariant rationale

`AtomicLong.getAndIncrement()` performs the allocation as one atomic transition
and returns the allocated value to the calling invocation. That local value is
passed to `sign` and used to construct the `Receipt`, so another caller's later
allocation cannot change the sequence/signature pairing. The signer is invoked
after allocation, with no generator monitor held, which lets independently safe
signer calls overlap.

The solution deliberately preserves a small but important semantic: a signer
failure or interruption consumes its already allocated value. Reusing it would
need a durable state machine and could create ambiguous audit history after a
partially completed signing operation.

## Resource use and complexity

Allocation is `O(1)` and the generator retains one atomic counter plus the
injected signer. Signing time and memory are defined by the signer. There is no
queue or cap on concurrent signer invocations, so callers can still overload a
limited downstream dependency. Under very high allocator contention the atomic
counter is a shared cache-coherence point, although the protected operation is
far smaller than the original signing interval.

## Failure and recovery behavior

An `InterruptedException` from the signer propagates unchanged and no receipt is
returned. A runtime failure likewise propagates. Neither case rolls the counter
back, retries, logs, or cancels work because the API does not define those
policies. A caller that retries receives a new sequence; production retry logic
must make the signer operation and its audit semantics explicit.

## Alternatives and rejected designs

A tiny `synchronized` block used only for allocation can also satisfy this
exercise, but an atomic counter expresses the single transition directly.
`LongAdder` is designed for scalable aggregate statistics and cannot give each
caller a unique prior value. Holding a lock around `sign` is correct only when
the signer itself requires that serialization, in which case the lock belongs to
the signer boundary and its capacity should be named and measured.

## Multi-instance implications

The counter is neither persistent nor shared. Separate JVMs and restarted
generators will reuse values. A database sequence, transactional outbox-style
workflow, or dedicated identifier service may solve a different global contract,
but none makes an external signature automatically atomic with number
allocation.

## Observability

This minimal class emits no metrics or logs. At its boundary, record allocation
count, signer concurrency, signer latency, failures, interruptions, and any
signer-admission rejections with correlation IDs that do not expose document
content. Those signals separate allocator pressure from downstream saturation.

## Security considerations

The document and signature are opaque strings here; this is not cryptographic
validation. Do not log sensitive documents or key material. A real signer needs
key authorization, protected credentials, integrity/audit controls, and a
careful policy for retries after uncertain remote outcomes. Thread safety of the
injected signer is a required integration contract, not something this class can
enforce.

## Limits

`long` eventually wraps and the implementation has no overflow policy. There is
no fairness, deadline, backpressure, durability, distributed uniqueness, or
guarantee that a completed signature was externally committed. The test proves
that the generator does not serialize two controlled signer calls; it does not
measure production throughput.

## Primary references

- [AtomicLong API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicLong.html)
- [Java concurrency utilities package summary, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)
- [OpenJDK JMH project](https://openjdk.org/projects/code-tools/jmh/)
