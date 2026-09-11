# Reduce Event Parser Allocation

**Category:** JVM memory and performance  
**Difficulty:** Specialist  
**Estimated time:** 105 minutes

## Scenario, symptoms, and business impact

A consumer parses a large event stream and short-lived parser objects cause frequent young-generation collection.

## Learning objectives and prerequisites

Preserve a strict wire contract while removing avoidable parser intermediates. Complete exercises 11 and 12 first.

## Underlying cause without revealing the solution

Regex splitting allocates a fields array and regex machinery for every input.

## System invariants

- `type|customerId|amount` has exactly three non-empty textual fields except that amount may be signed.
- Valid records preserve field values; malformed records throw `IllegalArgumentException`.
- The parser creates no intermediate fields array.

## Delivery and consistency guarantees

This parser is deterministic and local; it does not authenticate, version, or deliver events.

## Process-local versus distributed guarantees

Reducing allocation does not prove improved broker throughput or end-to-end latency.

## Functional requirements

Preserve `parse(String)` and the grammar, then make the package-local allocation probe remain zero.

## Non-functional requirements and resource limits

Use JMH GC-profiler evidence manually. CI has no score threshold.

## Constraints and forbidden shortcuts

Do not accept extra fields, use elapsed-time loops, weaken invalid-input handling, or change the returned record.

## Architecture or sequence diagram

```text
payload -> delimiter positions -> validation -> immutable Event
```

## Deterministic failure reproduction

The starter acceptance test observes its intermediate-array probe rather than heap timing.

## Task and automated acceptance criteria

Preserve valid and invalid behavior and avoid the intermediate array. Interpret benchmark results manually.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:13-reduce-event-parser-allocation:starter:starterAcceptanceTest
./gradlew verifyExercise13
./gradlew :exercises:13-reduce-event-parser-allocation:solution:jmhSmoke
./gradlew :exercises:13-reduce-event-parser-allocation:solution:jmh
```

## Progressive hints: observation, mechanism, design direction

1. Identify the temporary object whose lifetime ends before the returned event.
2. Find delimiters before extracting fields.
3. Validate structure before numeric conversion.

## Common wrong solutions and why they fail

- Removing validation changes the wire contract.
- A fast local score does not prove lower production latency.
- A custom `CharSequence` view can retain a large buffer; Java 21 `String.substring` copies characters.

## Production and emulator boundaries

Use JFR and realistic payload distributions to investigate allocation. Verify charset decoding, schema evolution, and backpressure separately.

## Specialist extension

Model a custom slice retaining a shared bulk buffer, then compare copying a small field with retaining the large payload.

## Interview follow-up questions

- Why can lower allocation improve throughput without improving tail latency?
- When is copying a field preferable to retaining a payload?
- Which parser behavior is a versioned schema contract?

## Reflection and future-post evidence

Capture the deterministic allocation-shape failure, a GC-profiler artifact, and retained versus temporary memory.

## Primary references

- [JMH samples](https://github.com/openjdk/jmh/tree/master/jmh-samples/src/main/java/org/openjdk/jmh/samples)
- [Java Flight Recorder API](https://docs.oracle.com/en/java/javase/21/jfapi/)
