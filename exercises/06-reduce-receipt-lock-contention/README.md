# Reduce Receipt Lock Contention

**Category:** Concurrency and performance measurement

Receipt sequence allocation is tiny, but the starter holds its monitor while a
potentially expensive signer runs. Under parallel load, unrelated receipts are
serialized. Preserve unique sequence/signature pairs while reducing the critical
section to sequence allocation.

The acceptance test uses controlled signing gates, not elapsed time: two signers
must enter concurrently. The solution uses an `AtomicLong` for unique sequence
allocation and invokes the supplied thread-safe signer after that transition.

Run correctness checks with:

```shell
./gradlew verifyExercise06
```

Run the manual JMH experiment with:

```shell
./gradlew :exercises:06-reduce-receipt-lock-contention:solution:jmh
```

JMH results are environment-dependent evidence, never a pass/fail throughput
claim. Compare warm measurements, allocation, contention, CPU frequency, and
the actual downstream signer before drawing a production conclusion.
