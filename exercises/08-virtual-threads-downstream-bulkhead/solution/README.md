# Reference Solution

The semaphore bounds calls that have crossed into the downstream dependency.
It is acquired interruptibly and released in `finally`, including when the
dependency fails. Virtual threads improve caller scalability, while the
bulkhead protects a distinct finite resource.
