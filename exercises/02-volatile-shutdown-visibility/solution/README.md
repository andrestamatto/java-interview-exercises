# Reference solution: safe publication of a shutdown signal

The solution declares `stopRequested` as `volatile`. The write in `requestStop`
and a read that observes it in `isStopRequested` form the required
happens-before relationship. The field is one-way, so there is no compound
read-modify-write invariant to protect.

This is deliberately not a claim about stopping arbitrary work: blocking I/O,
long calculations, request admission, task cancellation, and multi-instance
shutdown all require additional mechanisms. `volatile` is also not a substitute
for an atomic stock decrement; exercise 03 addresses that distinction.

## Correctness, resource, and failure analysis

`requestStop` is the only writer and it only writes `true`. A volatile write and
a volatile read that observes it establish the needed happens-before relation.
A reader racing before the request may legitimately see `false`, so workers must
check at cooperative checkpoints; because no API writes `false`, a caller that
has observed stop-requested cannot later observe running from this object. This
is visibility for one field, not atomicity for a compound state transition.

Both methods are O(1), allocation-free, non-blocking, and do not sleep or spin.
Volatile accesses impose ordering costs, appropriate for this infrequent signal
but not a blanket hot-path choice. The signal cannot interrupt I/O, cancel
remote work, drain requests, or roll back effects. Service shutdown additionally
needs admission control, deadlines, interruption/resource closure where
supported, bounded draining, and a lifecycle protocol; a process crash loses
this state.

## Alternatives, boundaries, and evidence

An ordinary boolean has no cross-thread visibility guarantee; sleep is not a
proof; and `volatile` cannot fix check-then-act logic. `synchronized` and
interruption can be valid elsewhere but change this focused publication lesson.
This signal cannot coordinate replicas. Measure request-to-acknowledgement time
and unfinished work with bounded labels; real shutdown callers require
authentication, authorization, and audit logging.

The acceptance test reflects on the field modifier, avoiding a lucky timed run.
jcstress reports the valid pre-request `false` and observed-request `true` race
orders, not a probabilistic stale-read gate.

```shell
./gradlew :exercises:02-volatile-shutdown-visibility:solution:check
./gradlew :exercises:02-volatile-shutdown-visibility:solution:jcstress
./gradlew verifyExercise02
```

- [JLS 17.4: Memory Model](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html)
- [JLS 17.4.5: happens-before order](https://docs.oracle.com/javase/specs/jls/se21/html/jls-17.html#jls-17.4.5)
- [jcstress project](https://openjdk.org/projects/code-tools/jcstress/)
