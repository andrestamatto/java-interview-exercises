# Index Membership Lookups

Nested membership scans turn reconciliation into `N×M` comparisons. Build an
index once and preserve the order and duplicates of the incoming requests.
Correctness uses deterministic output assertions; performance claims require
operation counts or JMH, never elapsed-time test gates.

The specialist extension instruments equality probes and compares the linear
scan with the indexed representation on the same input; no CI test asserts a
machine-dependent throughput number.
