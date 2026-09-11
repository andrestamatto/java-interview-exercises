# Bounded Import Executor

An import burst placed in an unbounded executor queue retains work until heap
pressure becomes an outage. Configure two workers, one queued task, explicit
rejection, and interruptible shutdown. The acceptance test blocks both workers,
fills the one-slot queue, then proves the next submission is rejected.

This is process-local admission control, not a durable queue or cross-node rate
limit. Run `./gradlew verifyExercise07`; the starter acceptance task is red
before the fix.
