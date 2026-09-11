# Reference Solution: Deadlock-Free Account Transfer

The solution calculates a stable order from the unique account IDs before
acquiring a lock. Every transfer therefore waits for the same first account,
which removes circular wait. It still uses `try/finally` so both locks are
released after insufficient funds, interruption, or a later implementation
failure.

`lockInterruptibly` is used for recoverable local shutdown. It is not a
substitute for the lock order: interruption is recovery, while stable ordering
is prevention.
