# Virtual Threads with a Downstream Bulkhead

Virtual threads make blocking callers cheap; they do not make a downstream
connection pool unlimited. The starter allows every virtual thread into a
controlled dependency. Preserve interruption and release a semaphore permit in
`finally` so failures cannot leak capacity.

The acceptance test starts four virtual-thread callers against a two-call
downstream budget. It measures in-flight calls through latches and counters,
not elapsed time. This is a local bulkhead, not a distributed rate limit.

Run `./gradlew verifyExercise08`. For diagnostics, capture Java 21 pinning with
`-Djdk.tracePinnedThreads=full`; Java 25 changes monitor-related pinning but not
the downstream capacity requirement.
