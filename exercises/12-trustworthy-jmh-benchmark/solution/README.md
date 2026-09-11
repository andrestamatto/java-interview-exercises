# Reference Solution

The solution isolates state per benchmark thread, supplies representative input through `@Param`, consumes each computed label with `Blackhole`, and uses warmup, measurement, forks, average-time mode, nanoseconds, and JSON output. This prevents the exercise from accidentally measuring an eliminated expression or shared-state contention.

The score remains an environmental observation. Compare JSON artifacts only with JVM, CPU, flags, input mix, allocation/GC evidence, and uncertainty recorded; use a load test for end-to-end claims.
