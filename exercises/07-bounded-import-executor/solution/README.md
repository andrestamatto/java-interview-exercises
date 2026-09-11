# Reference Solution: Bounded Import Executor

The solution has two workers and one queue slot. `AbortPolicy` makes saturation
explicit; `submit` converts only that admission decision into `false`. Work is
not silently retained in an unbounded heap queue. `shutdownNow` is a local
interruption policy, so real import work must cooperate with interruption and
must not be treated as a durable messaging contract.
