# Reference Solution

`ContextTask` captures the submitting thread's tenant once, installs it only while the delegate runs, and removes the worker-local value in `finally`. This prevents stale context on a reused worker even if the delegate throws.

The implementation is intentionally small because this exercise has one immutable context field. It is constant-size work per wrapped task and is not a distributed context protocol, an authorization decision, or a replacement for OpenTelemetry/MDC integration. `InheritableThreadLocal` only copies state when a new thread is created, so it does not solve executor reuse. Production logs must redact sensitive identifiers and alert on missing correlation fields.
