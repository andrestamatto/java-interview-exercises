# Reference solution: conditional quota transition

The solution retries a compare-and-set only while a positive value remains. Each
failed CAS proves that another caller changed the state, so the loop reloads and
re-evaluates availability. A successful CAS consumes exactly one permit.

This is a lock-free process-local transition, not a fair distributed rate
limiter. A production tenant quota needs a shared authority, state expiry,
idempotency, authorization, and observability of rejections and contention.
