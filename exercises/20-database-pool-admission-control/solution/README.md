# Reference Solution: Short Transactions Behind Local Admission

`CaptureService` uses an immediate `Semaphore.tryAcquire()` before it calls the
remote risk gateway. A rejected request has no database side effect and cannot
join an unbounded queue of connection waiters. The permit is retained for the
whole admitted attempt so the process has a fixed upper bound on active capture
work, but `TransactionPool.acquire()` happens only after risk approval.

`recordApprovedCapture` owns a short transaction with try-with-resources. Any
failure before a successful commit rolls back in `finally`, then closes the
connection. The close returns a healthy connection to Hikari or allows Hikari
to discard a broken one. The deterministic contract proves a subsequent
capture can use the released capacity.

`CommitOutcomeUnknownException` is handled separately. A failed acknowledgement
from `commit` may follow a durable server-side commit, so the service returns
`COMMIT_OUTCOME_UNKNOWN`, does not rollback through that unknown connection,
and does not retry. A caller needs an idempotency/reconciliation design before
another attempt. This is intentionally stronger than classifying all runtime
errors as retryable.

The JDBC adapter is deliberately small: it converts connection lifecycle and
SQL exceptions into the service's transaction boundary, while policy remains in
the application class. It is not a replacement for Spring transaction
management in a larger application.

The semaphore and Hikari pool are local resources. Instrument attempted,
admitted, capacity-rejected, pool-acquire-failed, rollback, commit-unknown,
and connection-wait duration signals with bounded operation labels. Do not use
capture IDs as metric tags. Production sizing also needs database limits,
thread/virtual-thread limits, downstream deadlines, replica count, and SLOs.

See the exercise README for exact commands and emulator boundaries.
