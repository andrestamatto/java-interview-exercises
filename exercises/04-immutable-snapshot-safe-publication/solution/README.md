# Reference solution: ownership transfer at publication

`DiscountSnapshot` validates and copies the incoming map with `Map.copyOf`.
The catalog then publishes that complete immutable value with `AtomicReference`.
The copy is made once per update, so later caller mutations cannot alter the
snapshot and readers need neither a lock nor a defensive copy.

This does not coordinate replicas or provide an audit trail. A production pricing
system still needs versioning, authorization, rollout policy, durable storage,
and cross-instance propagation.

## Correctness, resource, and failure analysis

`Map.copyOf` makes an unmodifiable copy rather than an unmodifiable view of the
caller map. The actual keys and values are immutable `String` and `Integer`, so
the snapshot is deeply immutable for this domain; mutable value types would need
their own defensive copies. Validation and copying occur before the atomic
`set`, so a bad draft preserves the previous snapshot. Readers see one complete
snapshot, before or after publication, and a caller's later mutation cannot
alter it.

Publishing n rules is O(n) time and memory; lookup is an expected O(1),
allocation-free, lock-free map lookup. This suits a read-heavy catalog but large
or frequent publications need a measured memory/GC budget. Invalid drafts fail
before publication, while restart, durable version history, rollback, and
cross-replica convergence remain intentionally absent.

## Alternatives, boundaries, and evidence

A record alone retains aliases, `Collections.unmodifiableMap` follows source
mutations, and a volatile map reference cannot prevent alias mutation. A
read-write lock is a different workload trade-off. Each JVM may publish a
different version. Production rule publishing needs authenticated and authorized
authors, audit records, input-size limits, and no full rule maps in logs/metrics;
use bounded signals such as validation failures, rule count, update duration,
version, and replica age.

The acceptance test publishes 10%, mutates the retained draft to 90%, and then
releases a reader, which must still receive 10. Unit tests cover validation and
missing products.

```shell
./gradlew :exercises:04-immutable-snapshot-safe-publication:solution:check
./gradlew verifyExercise04
```

- [AtomicReference API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/concurrent/atomic/AtomicReference.html)
- [Map.copyOf API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/Map.html#copyOf(java.util.Map))
- [JLS 17.4: Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
