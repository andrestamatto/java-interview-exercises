# Reduce Event Parser Allocation

**Category:** JVM memory and performance  
**Difficulty:** Specialist  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

A consumer parses a large event stream. Its `split` call creates short-lived regex and array intermediates for every payload, increasing young-generation allocation and collection work.

## Learning objectives and prerequisites

Preserve a strict wire contract while removing one avoidable parser intermediate. Complete exercises 11 and 12 first.

## Underlying cause without revealing the solution

Regex splitting allocates a fields array and performs regex-oriented work even though this wire format has one fixed delimiter.

## System invariants

- A valid payload is `type|customerId|amount`: exactly two delimiters, non-empty type and customer ID, and an amount accepted by `Long.parseLong` (including a leading sign).
- Valid records preserve the two text fields and numeric amount; malformed input throws `IllegalArgumentException`.
- The implementation creates no intermediate fields array.
- The public `parse(String)` path must not allocate a `ParserAllocationProbe`; the package-local overload exists only for deterministic acceptance instrumentation.

## Delivery and consistency guarantees

This parser is deterministic and local. It does not authenticate, version, persist, or deliver events.

## Process-local versus distributed guarantees

Removing a local allocation does not establish broker throughput, end-to-end latency, ordering, backpressure, or schema compatibility across services.

## Functional requirements

Keep `parse(String)` and the grammar. Find the delimiters, reject malformed structure, convert the amount, and leave the probe at zero when the package-local test overload is used.

## Non-functional requirements and resource limits

Use JMH GC-profiler evidence manually. CI has no speed or allocation score threshold. The goal is the absence of the known intermediate array, not a claim that all allocations disappear: the returned `Event` necessarily retains its text fields.

## Constraints and forbidden shortcuts

Do not accept extra fields, change invalid-input behavior, use elapsed-time loops, alter the returned record, or retain a test probe on the public parsing path.

## Architecture or sequence diagram

```text
payload -> delimiter positions -> structure validation -> field extraction + long conversion -> Event
```

## Deterministic failure reproduction

The starter acceptance test observes its package-local intermediate-array probe rather than heap timing. The solution must make that probe remain zero without making ordinary callers allocate it.

## Task and automated acceptance criteria

Preserve valid and invalid behavior; reject missing, empty, extra, and non-numeric fields; and avoid the intermediate array. Interpret JMH results manually.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:13-reduce-event-parser-allocation:starter:starterAcceptanceTest
./gradlew verifyExercise13
./gradlew :exercises:13-reduce-event-parser-allocation:solution:jmhSmoke
./gradlew :exercises:13-reduce-event-parser-allocation:solution:jmh
```

## Progressive hints: observation, mechanism, design direction

1. Identify the temporary object whose lifetime ends before the returned event.
2. Locate both delimiters before extracting any field.
3. Validate structure before converting the numeric suffix.
4. Keep test-only instrumentation off the public hot path.

## Common wrong solutions and why they fail

- Removing validation changes the wire contract.
- Keeping `split` or creating an equivalent `String[]` leaves the targeted allocation shape intact.
- A fast local score does not prove lower production latency.
- A custom `CharSequence` view can retain a large buffer; Java 21 `String.substring` copies characters.

## Production and emulator boundaries

Use JFR and realistic payload distributions to investigate allocation. Verify payload-size limits, charset decoding, schema evolution, malformed-event routing, and backpressure separately.

## Specialist extension

Model a custom slice retaining a shared bulk buffer, then compare copying a small field with retaining the large payload. Use a schema version field and make the compatibility rule explicit.

## Interview follow-up questions

- Why can lower allocation improve throughput without improving tail latency?
- When is copying a field preferable to retaining a payload?
- Which parser behavior is a versioned schema contract?
- Why is a deterministic allocation-shape probe different from a memory profile?

## Reflection and future-post evidence

Capture the deterministic intermediate-array failure, a GC-profiler artifact, the public-path instrumentation change, and the distinction between temporary allocation and retained memory.

## Primary references

- [String API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/String.html)
- [Long.parseLong API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/Long.html#parseLong(java.lang.String))
- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Java Flight Recorder API](https://docs.oracle.com/en/java/javase/21/jfapi/)
