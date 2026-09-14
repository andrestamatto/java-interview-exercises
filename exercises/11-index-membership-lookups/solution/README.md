# Reference Solution

## Rationale

The solution builds one `HashSet` from `grants`, then traverses `requests` in its original order. Each request is appended only when the index contains it. Traversing the request list, rather than converting it to a set, is what preserves both order and duplicate requests.

The starter asks the same membership question by comparing each request against every grant. The index turns that repeated scan into a lookup. `ComparisonCounter` is package-private acceptance instrumentation; the indexed implementation has no nested equality loop to record, so it does not invoke the counter.

## Resources and complexity

For `g` grants and `r` requests, construction plus lookup is expected `O(g + r)` time and `O(g)` additional space. `HashSet` provides expected, not hard real-time, constant-time membership: collisions, resizing, allocation pressure, and adversarial hash codes can change observed latency. The result itself uses up to `O(r)` space, as required by the output.

## Failure handling and recovery

This is a pure in-memory transformation: it has no partial external effect to roll back. A caller that needs an updated answer after grants change must call it again with a fresh, authoritative grant list; this implementation intentionally does not cache or invalidate an index. Input validation, null-element policy, and error translation belong to the caller because the exercise API does not define them.

## Alternatives and trade-offs

- A sorted immutable grant representation with binary search uses less overhead in some workloads but costs `O(log g)` per lookup and needs an ordered input or a sorting step.
- A database join or a service-side authorization index is appropriate when the source of truth is remote; it changes consistency, latency, and failure behavior.
- A Bloom filter can cheaply reject most non-members, but false positives require an authoritative follow-up check.
- Reusing a cached `HashSet` can avoid rebuilds only when ownership, freshness, and tenant scope are rigorously controlled.

## Multi-instance boundary

There is no replication, cache invalidation, tenant partitioning, or authorization decision here. In a multi-instance system, grant freshness and revocation semantics must come from the durable authorization source. A locally cached index is never by itself sufficient to enforce access.

## Observability and security

Measure input cardinalities, match ratio, allocation, and CPU with representative data; the deterministic comparison probe describes algorithm shape, not production latency. Do not emit raw grants or product identifiers in high-cardinality metrics. Apply tenant and permission checks before constructing the relevant grant set, and treat a positive membership result as one input to authorization rather than authorization itself.

## Limits

The implementation is single-threaded with respect to the supplied lists and relies on their normal collection semantics during the call. It does not mutate either input, but it does not make a concurrent mutation of an input list safe. Hash-based complexity is probabilistic/expected, and a large per-request index may be the wrong memory trade-off.

## Primary references

- [HashSet API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashSet.html)
- [HashMap API](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/util/HashMap.html)
