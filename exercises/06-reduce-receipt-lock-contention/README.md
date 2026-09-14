# Reduce Receipt Lock Contention

- **Category:** Concurrency and performance measurement
- **Difficulty:** Advanced
- **Estimated time:** 70 minutes

## Scenario, symptoms, and business impact

A receipt service assigns a sequence number and asks a signer to produce a
signature for a document. The signer may be slow: it can represent cryptographic
work, a hardware security module, or a remote dependency. In the starter, an
unrelated receipt cannot begin signing while another call is in progress. Under
load, receipt latency and the caller backlog grow even when the signer itself
can serve concurrent requests.

The business risk is artificial serialization of a path that should have a tiny
state transition followed by independent work. It consumes request threads,
makes a healthy downstream signer look slow, and reduces the service's useful
throughput.

## Learning objectives and prerequisites

- Identify the state that actually requires mutual exclusion.
- Preserve the relationship between an allocated sequence and its signature.
- Separate a deterministic concurrency test from a throughput experiment.
- Explain why a process-local atomic operation is not a distributed sequence
  authority.

Complete exercises 01--05 first, especially the distinction between visibility,
atomicity, and lock scope. Familiarity with `AtomicLong` and JMH is helpful.

## Underlying cause without revealing the solution

The starter protects a mutable sequence with a method monitor, but the monitor
also covers the supplied signer's potentially blocking call. A monitor protects
all code in its critical section, not just the field that motivated it. The
result is correct sequence allocation at the cost of serializing work that does
not need to share that field.

## System invariants

- Within one `ReceiptGenerator` lifetime and before `long` overflow, no two
  allocations use the same sequence.
- A returned `Receipt` contains the exact sequence passed to its corresponding
  `ReceiptSigner.sign(sequence, document)` invocation.
- Signer execution must not hold the sequence-allocation critical section.
- An interrupted signing operation propagates `InterruptedException`; it does
  not manufacture a receipt.

## Delivery and consistency guarantees

`issue` is a synchronous, in-memory operation. A successful return means only
that the supplied signer returned a `String` for the allocated sequence. If
signing fails or is interrupted after allocation, the sequence may be consumed
without a returned receipt; this exercise does not promise gap-free numbering,
durability, retries, or exactly-once signing.

## Process-local versus distributed guarantees

The sequence belongs only to this generator instance. A restart starts at one,
and two instances can allocate the same values. A globally unique or durable
receipt number needs an external authority, such as a database sequence,
transactional allocation record, or a deliberately designed distributed ID
scheme. Moving the Java synchronization primitive does not create that
guarantee.

## Functional requirements

- Keep the public `ReceiptGenerator`, `Receipt`, and `ReceiptSigner` contract.
- Allocate sequences beginning at one for a new generator.
- Return a receipt whose signature was calculated for that receipt's sequence
  and document.
- Permit two callers blocked inside a concurrent-safe signer to enter that
  signer concurrently.
- Reject a null signer at construction, as the existing API does.

## Non-functional requirements and resource limits

Sequence allocation should be constant-time and should not retain a queue of
documents. The exercise does not bound concurrent calls to `sign`: if the real
signer has a concurrency, connection, or rate limit, add a separate admission
policy at that boundary. Measure any throughput claim with representative signer
work, warm-up, allocation data, CPU conditions, and contention data; JMH is not
an acceptance test.

## Constraints and forbidden shortcuts

- Do not change the supplied signer into a different synchronous API.
- Do not hold a monitor or another exclusive lock while invoking `sign`.
- Do not use sleeps, elapsed throughput, or processor count as correctness
  evidence.
- Do not use `LongAdder`: its aggregate value is not an allocation operation
  that returns a unique sequence to each caller.
- Do not claim cross-process uniqueness or a signature's cryptographic validity.

## Architecture or sequence diagram

```text
caller A -- allocate sequence A --+--> signer(A, document A) --> Receipt(A, signature A)
                                  |
caller B -- allocate sequence B --+--> signer(B, document B) --> Receipt(B, signature B)

The allocation transition is shared; signer calls are independent of it.
```

## Deterministic failure reproduction

`ReceiptGeneratorAcceptanceTest` supplies a signer that records entry, then
waits on a latch. It starts two callers and waits for both signer entries. The
starter allows only one entry because the first call still owns its method
monitor. The latch expresses the required state directly; the timeout is only a
test liveness bound, not a performance measurement.

## Task and automated acceptance criteria

Change only the generator implementation so that the invariants hold. The
acceptance test requires both controlled signer calls to enter before either is
released. The ordinary test checks the first two sequence/signature pairs. Keep
the starter independently buildable and keep its acceptance defect behavioral,
not a compilation failure.

## Exact build, test, run, and benchmark commands

Run these commands from the repository root:

```shell
./gradlew :exercises:06-reduce-receipt-lock-contention:starter:compileJava
./gradlew :exercises:06-reduce-receipt-lock-contention:starter:starterAcceptanceTest
./gradlew :exercises:06-reduce-receipt-lock-contention:solution:test
./gradlew verifyExercise06
./gradlew :exercises:06-reduce-receipt-lock-contention:solution:jmh
```

There is no standalone application. The last command runs the manual JMH
comparison and writes its configured JSON result under the solution build
directory; it is not a correctness gate.

## Progressive hints: observation, mechanism, design direction

1. Mark the smallest statement that mutates shared receipt state.
2. Ask whether the signer reads or mutates that state after it receives its
   arguments.
3. Make sequence allocation one atomic transition, copy its result into a local
   variable, and perform signing after that transition.

## Common wrong solutions and why they fail

- Keeping `synchronized` on all of `issue` preserves uniqueness but keeps the
  expensive serialization.
- Reading and incrementing a plain `long` without synchronization loses updates.
- Allocating a sequence but accidentally signing with shared mutable state can
  pair the wrong signature with a receipt under concurrency.
- Replacing the monitor with a global signer lock merely moves the same
  contention unless the signer itself requires serialization.
- Using a benchmark score as proof of correctness misses pairing and liveness
  failures.

## Production and emulator boundaries

The test signer is a latch-based emulator, not a cryptographic implementation,
HSM, network client, durable counter, or retry protocol. A production design
must state signer thread safety and capacity, request deadlines, error handling,
key isolation, audit requirements, sequence persistence, and its behavior after
a partial failure. Those policies are intentionally outside this small API.

## Specialist extension

Add an explicitly bounded signer adapter and expose its wait and rejection
signals. Then use a production-like signer stub with variable latency in JMH and
compare a narrowly locked allocator with the original broad lock. State the
chosen durability and global-ID contract before attempting a multi-instance
implementation.

## Interview follow-up questions

- Why is a sequence gap after a failed signing attempt sometimes preferable to
  reusing the sequence?
- When would a small synchronized allocation block be an acceptable design?
- How would you make number allocation durable without claiming that signing is
  transactional with it?
- Which metrics distinguish allocator contention from signer saturation?

## Reflection and future-post evidence

Capture the controlled test's single-entry symptom and the repaired two-entry
state. Explain the invariant that protects sequence/signature pairing, then
share JMH configuration and environment alongside any measurement rather than a
portable throughput number. A useful post also states that the solution is
process-local and does not make an HSM or distributed counter scalable by
itself.

## Primary references

- [AtomicLong API, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicLong.html)
- [Java concurrency utilities package summary, Java 21](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/package-summary.html)
- [OpenJDK JMH project](https://openjdk.org/projects/code-tools/jmh/)
