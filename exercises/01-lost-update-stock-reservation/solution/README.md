# Reference solution: atomic conditional transition

The solution stores remaining stock in `AtomicInteger` and uses a compare-and-set
loop. A successful CAS is the successful reservation's linearization point. A
read that observes insufficient stock is the rejection's linearization point.

After a failed CAS, the command reloads the value and re-evaluates availability.
This matters: retrying a subtraction derived from stale state can violate the
business rule. The successful transition is atomic and `AtomicInteger` supplies
the required visibility for `remainingUnits()`.

The algorithm is lock-free, not wait-free: a failed CAS means another caller made
progress, but one caller may repeatedly lose. Work is O(1) without contention
and O(r) for r failed retries. Under extreme contention a monitor may use fewer
CPU cycles; measure that separately instead of assuming CAS is always faster.

`volatile`, separate atomic `get`/`set`, `LongAdder`, and decrement-then-rollback
do not implement the required conditional transition. A synchronized instance
method could be correct, but demonstrates a different progress mechanism.

The guarantee ends at this object in this JVM. Production replicas need a shared
authority such as a database conditional update or version check. Durable
reservations also need identity, expiry/cancellation rules, retry idempotency,
authorization, auditability, and operational signals such as conflict/retry
rates. Never record SKU or customer data in unbounded metric labels.
