# Reference Solution

The solution finds delimiters, validates the complete grammar, and only then creates the three strings required by immutable `Event`. It removes `split`'s temporary array while preserving valid records and invalid-input behavior.

The implementation is linear in payload size. The package-local probe is deterministic acceptance instrumentation, not a memory profiler. JMH GC-profiler output provides allocation evidence but not an end-to-end latency guarantee. Java 21 `String.substring` copies characters, so large backing-buffer retention is accurately kept as a separate custom-slice extension.
