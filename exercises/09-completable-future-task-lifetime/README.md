# CompletableFuture Task Lifetime

A successful fan-out result can leave its losing sibling running. Make task
ownership explicit: once the parent result is terminal, cancel children that no
longer have a useful owner. Cancellation is cooperative and does not prove that
an arbitrary remote call stopped; callers still need deadlines and idempotency.
