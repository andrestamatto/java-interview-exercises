# Immutable Snapshot Safe Publication

**Category:** Safe publication and immutability
**Difficulty:** Advanced
**Estimated time:** 90 minutes

## Scenario, symptoms, and business impact

Pricing publishes a discount snapshot while retaining the mutable map used to
build it. A later edit silently changes the rule seen by checkout readers.

## Learning objectives and prerequisites

Create a deeply immutable snapshot and publish it atomically. Complete exercise
02 first to understand the visibility requirement.

## Underlying cause

A record is only shallowly immutable: its map component can still point at a
mutable caller-owned object. Publishing that reference makes later mutation part
of the catalog state.

## System invariants

- A published snapshot never changes.
- Discounts are between 0 and 100.
- A lookup observes either an older complete snapshot or a newer complete one.

## Delivery and consistency guarantees

`AtomicReference.set/get` safely publishes a complete immutable snapshot. A
reader never observes a partially constructed map through this API.

## Process-local versus distributed guarantees

This is one JVM catalog, not cross-node cache invalidation or a transactional
pricing rollout.

## Functional requirements

Preserve the API and validation. A caller changing its draft after `publish`
must not change the already published catalog.

## Non-functional requirements and resource limits

Copy only at publication; reads should remain allocation-free and lock-free.

## Constraints and forbidden shortcuts

Do not retain a mutable map, return it for mutation, synchronize every read, or
change the test's ordering with sleeps.

## Architecture or sequence diagram

```text
caller draft -> defensive copy -> immutable snapshot -> AtomicReference -> reader
```

## Deterministic failure reproduction

The test publishes a draft, mutates it, then releases a reader. The starter
observes 90 instead of the published 10; the solution retains 10.

## Task and automated acceptance criteria

Fix the starter so the retained-mutation scenario and validation tests pass.

## Exact build, test, run, and benchmark commands

```shell
./gradlew :exercises:04-immutable-snapshot-safe-publication:starter:starterAcceptanceTest
./gradlew :exercises:04-immutable-snapshot-safe-publication:solution:check
./gradlew verifyExercise04
```

The starter acceptance test is intentionally red before the change. There is no
benchmark claim.

## Progressive hints

1. **Observation:** determine who owns the map after publication.
2. **Mechanism:** inspect the difference between a record and an immutable map.
3. **Design direction:** create the copy at the ownership boundary.

## Common wrong solutions and why they fail

- A record alone does not copy mutable components.
- `Collections.unmodifiableMap` is only a read-only view of a mutable source.
- `volatile` does not stop later mutation through another alias.

## Production and emulator boundaries

There is no durable history, version rollout, authorization, cache replication,
or multi-instance consistency protocol here.

## Specialist extension

Add versioned snapshots and reader metrics; compare copy-on-write publication
with a read-write lock for a realistic update/read ratio.

## Interview follow-up questions

- What makes an object deeply immutable?
- Why is an unmodifiable view insufficient?
- What does `AtomicReference` guarantee here?
- How would snapshots propagate across service instances?

## Reflection and future-post evidence

Record the retained-alias bug, the publication boundary, and the exact scope of
the guarantee.

## Primary references

- [AtomicReference API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicReference.html)
- [Map.copyOf API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html#copyOf(java.util.Map))
- [JLS 17.4: Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
