# Reference Solution

The solution builds one `HashSet` from grants, then scans requests in their original order. Membership is expected constant time on average, so the operation is expected `O(grants + requests)` time and uses `O(grants)` extra space. The request scan deliberately remains a list traversal because replacing it with a set would discard order and duplicates.

The operation counter is package-private test instrumentation, not part of the production API. It proves the intended algorithmic shape deterministically and does not claim a latency improvement on every machine or for adversarial hash distributions. A production authorization system must still enforce tenant scope and make its own freshness and cache-consistency guarantees.
